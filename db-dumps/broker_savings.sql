--
-- PostgreSQL database dump
--

-- Dumped from database version 17.4
-- Dumped by pg_dump version 17.4

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: deposit_balance(numeric, integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.deposit_balance(IN p_amount numeric, IN p_currency_id integer, IN p_customer_account_id integer)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'public', 'pg_catalog'
    AS $$
declare
    v_savings_account_id integer;
begin
    if p_amount <= 0 then
        raise exception 'Deposit amount must be positive';
    end if;

    -- ищем существующий счет
    select id
    into v_savings_account_id
    from savings_accounts
    where customer_account_id = p_customer_account_id
      and currency_id = p_currency_id;

    if v_savings_account_id is null then
        -- создаем новый счет
        insert into savings_accounts (
            customer_account_id,
            savings_account_number,
            currency_id,
            balance,
            reserved_amount
        )
        values (
                   p_customer_account_id,
                   concat('ACC-', p_customer_account_id, '-', p_currency_id, '-', extract(epoch from now())),
                   p_currency_id,
                   p_amount,
                   0
               )
        returning id into v_savings_account_id;
    else
        -- обновляем баланс
        update savings_accounts
        set balance = balance + p_amount
        where id = v_savings_account_id;
    end if;

    -- пишем в историю
    insert into balance_history (savings_account_id, transaction_date, amount, transaction_type)
    values (v_savings_account_id, now(), p_amount, 1);
end;
$$;


ALTER PROCEDURE public.deposit_balance(IN p_amount numeric, IN p_currency_id integer, IN p_customer_account_id integer) OWNER TO postgres;

--
-- Name: generate_savings_account_number(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.generate_savings_account_number() RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    account_number varchar(30);
    length int;
    i int;
    digit int;
    exists boolean;
BEGIN
    -- Генерируем случайную длину от 10 до 30
    length := 10 + floor(random() * 21)::int;

    LOOP
        account_number := '';
        -- Генерируем случайный номер заданной длины
        FOR i IN 1..length LOOP
                digit := floor(random() * 10)::int;
                account_number := account_number || digit::varchar;
            END LOOP;

        -- Проверяем, существует ли уже такой номер
        SELECT EXISTS (
            SELECT 1 FROM savings_accounts WHERE savings_account_number = account_number
        ) INTO exists;

        -- Если номер уникален, выходим из цикла
        IF NOT exists THEN
            EXIT;
        END IF;
    END LOOP;

    RETURN account_number;
END;
$$;


ALTER FUNCTION public.generate_savings_account_number() OWNER TO postgres;

--
-- Name: withdraw_balance(numeric, integer, integer); Type: PROCEDURE; Schema: public; Owner: postgres
--

CREATE PROCEDURE public.withdraw_balance(IN p_amount numeric, IN p_currency_id integer, IN p_customer_account_id integer)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'public', 'pg_catalog'
    AS $$
declare
    v_savings_account_id integer;
    current_balance decimal(15,2);
    current_reserved_amount decimal(15,2);
begin
    -- сумма должна быть положительной
    if p_amount <= 0 then
        raise exception 'Withdrawal amount must be positive';
    end if;

    -- ищем счет
    select id, balance, coalesce(reserved_amount, 0)
    into v_savings_account_id, current_balance, current_reserved_amount
    from savings_accounts
    where customer_account_id = p_customer_account_id
      and currency_id = p_currency_id;

    if v_savings_account_id is null then
        raise exception 'Savings account not found for customer_account_id % and currency_id %',
            p_customer_account_id, p_currency_id;
    end if;

    -- проверка на достаточность средств
    if current_balance - p_amount < 0 then
        raise exception 'Insufficient funds: withdrawal would result in negative balance';
    end if;

    if current_balance - p_amount <= current_reserved_amount then
        raise exception 'Insufficient funds: withdrawal would violate reserved amount constraint';
    end if;

    -- обновляем баланс
    update savings_accounts
    set balance = balance - p_amount
    where id = v_savings_account_id;

    -- пишем в историю
    insert into balance_history (savings_account_id, transaction_date, amount, transaction_type)
    values (v_savings_account_id, now(), p_amount, 2);
end;
$$;


ALTER PROCEDURE public.withdraw_balance(IN p_amount numeric, IN p_currency_id integer, IN p_customer_account_id integer) OWNER TO postgres;

--
-- Name: balance_history_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.balance_history_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.balance_history_id_seq OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: balance_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.balance_history (
    id bigint DEFAULT nextval('public.balance_history_id_seq'::regclass) NOT NULL,
    savings_account_id integer NOT NULL,
    transaction_type smallint NOT NULL,
    transaction_date timestamp without time zone DEFAULT now() NOT NULL,
    amount numeric(15,2) NOT NULL,
    CONSTRAINT balance_history_amount_check CHECK ((amount > (0)::numeric))
);


ALTER TABLE public.balance_history OWNER TO postgres;

--
-- Name: currencies_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.currencies_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.currencies_id_seq OWNER TO postgres;

--
-- Name: currencies; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.currencies (
    id integer DEFAULT nextval('public.currencies_id_seq'::regclass) NOT NULL,
    code character varying(3) NOT NULL,
    CONSTRAINT chk_currency_code CHECK ((char_length((code)::text) = 3))
);


ALTER TABLE public.currencies OWNER TO postgres;

--
-- Name: processed_operations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.processed_operations (
    operation_id uuid NOT NULL
);


ALTER TABLE public.processed_operations OWNER TO postgres;

--
-- Name: savings_accounts_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.savings_accounts_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.savings_accounts_id_seq OWNER TO postgres;

--
-- Name: savings_accounts; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.savings_accounts (
    id integer DEFAULT nextval('public.savings_accounts_id_seq'::regclass) NOT NULL,
    customer_account_id integer NOT NULL,
    currency_id integer NOT NULL,
    savings_account_number character varying(30) NOT NULL,
    balance numeric(15,2) NOT NULL,
    reserved_amount numeric(19,5) DEFAULT 0 NOT NULL,
    CONSTRAINT chk_balance_reserved CHECK ((balance >= reserved_amount)),
    CONSTRAINT savings_accounts_balance_check CHECK ((balance >= (0)::numeric))
);


ALTER TABLE public.savings_accounts OWNER TO postgres;

--
-- Name: transaction_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transaction_types (
    id smallint NOT NULL,
    transaction_type character varying(20) NOT NULL
);


ALTER TABLE public.transaction_types OWNER TO postgres;

--
-- Data for Name: balance_history; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.balance_history (id, savings_account_id, transaction_type, transaction_date, amount) FROM stdin;
\.


--
-- Data for Name: currencies; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.currencies (id, code) FROM stdin;
1	USD
2	EUR
3	GBP
4	JPY
5	CHF
6	RUB
\.


--
-- Data for Name: processed_operations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.processed_operations (operation_id) FROM stdin;
\.


--
-- Data for Name: savings_accounts; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.savings_accounts (id, customer_account_id, currency_id, savings_account_number, balance, reserved_amount) FROM stdin;
1	1	1	47281930475628104957	100000000.00	0.00000
2	2	1	91827364501928374650	100000000.00	0.00000
3	3	1	56473829100485736291	100000000.00	0.00000
4	4	1	38291047562849301576	100000000.00	0.00000
5	5	1	81726354019827364590	100000000.00	0.00000
6	6	1	62538491706281937450	100000000.00	0.00000
7	7	1	93820184756392018475	100000000.00	0.00000
8	8	1	10485736291827364509	100000000.00	0.00000
9	9	1	75648392018475639201	100000000.00	0.00000
10	10	1	28374650192837465019	100000000.00	0.00000
11	11	1	64738291004756281930	100000000.00	0.00000
12	12	1	39018475628392018475	100000000.00	0.00000
13	13	1	81927364502819374650	100000000.00	0.00000
14	14	1	56381927465928104756	100000000.00	0.00000
15	15	1	10293847561029384756	100000000.00	0.00000
16	16	1	84756192038475619203	100000000.00	0.00000
17	17	1	39281746501928374650	100000000.00	0.00000
18	18	1	56473820194756382019	100000000.00	0.00000
19	19	1	91827304562819374650	100000000.00	0.00000
20	20	1	47382910475628104937	100000000.00	0.00000
21	21	1	81740293847561029384	100000000.00	0.00000
22	22	1	62819374650192837465	100000000.00	0.00000
23	23	1	38475619203847561920	100000000.00	0.00000
24	24	1	10928374651029384756	100000000.00	0.00000
25	25	1	75628391047562839104	100000000.00	0.00000
26	26	1	94736281904563728190	100000000.00	0.00000
27	27	1	61029384756102938475	100000000.00	0.00000
28	28	1	28593018475629301847	100000000.00	0.00000
29	29	1	83920184756392018475	100000000.00	0.00000
30	30	1	50192837465019283746	100000000.00	0.00000
\.


--
-- Data for Name: transaction_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.transaction_types (id, transaction_type) FROM stdin;
1	DEPOSIT
2	WITHDRAWAL
3	SENT
4	RECEIVED
\.


--
-- Name: balance_history_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.balance_history_id_seq', 107, true);


--
-- Name: currencies_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.currencies_id_seq', 1, false);


--
-- Name: savings_accounts_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.savings_accounts_id_seq', 30, true);


--
-- Name: balance_history balance_history_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.balance_history
    ADD CONSTRAINT balance_history_pkey PRIMARY KEY (id);


--
-- Name: currencies currencies_code_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.currencies
    ADD CONSTRAINT currencies_code_key UNIQUE (code);


--
-- Name: currencies currencies_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.currencies
    ADD CONSTRAINT currencies_pkey PRIMARY KEY (id);


--
-- Name: processed_operations processed_operations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.processed_operations
    ADD CONSTRAINT processed_operations_pkey PRIMARY KEY (operation_id);


--
-- Name: savings_accounts savings_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.savings_accounts
    ADD CONSTRAINT savings_accounts_pkey PRIMARY KEY (id);


--
-- Name: savings_accounts savings_accounts_savings_account_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.savings_accounts
    ADD CONSTRAINT savings_accounts_savings_account_number_key UNIQUE (savings_account_number);


--
-- Name: transaction_types transaction_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_types
    ADD CONSTRAINT transaction_types_pkey PRIMARY KEY (id);


--
-- Name: transaction_types transaction_types_transaction_type_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_types
    ADD CONSTRAINT transaction_types_transaction_type_key UNIQUE (transaction_type);


--
-- Name: savings_accounts uq_customer_currency; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.savings_accounts
    ADD CONSTRAINT uq_customer_currency UNIQUE (customer_account_id, currency_id);


--
-- Name: idx_balance_account; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_balance_account ON public.balance_history USING btree (savings_account_id);


--
-- Name: idx_savings_currency; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_savings_currency ON public.savings_accounts USING btree (currency_id);


--
-- Name: idx_savings_customer; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_savings_customer ON public.savings_accounts USING btree (customer_account_id);

--
-- PostgreSQL database dump complete
--

