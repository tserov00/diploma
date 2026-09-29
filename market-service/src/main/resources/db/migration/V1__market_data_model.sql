DROP TABLE IF EXISTS stocks CASCADE;
DROP TABLE IF EXISTS securities CASCADE;
DROP TABLE IF EXISTS security_types CASCADE;
DROP TABLE IF EXISTS stock_exchanges CASCADE;

CREATE TABLE securities (
    id BIGINT PRIMARY KEY,
    ticker VARCHAR(20) NOT NULL UNIQUE,
    isin VARCHAR(12),
    company_name VARCHAR(120) NOT NULL,
    exchange_code VARCHAR(10) NOT NULL,
    currency_code VARCHAR(3) NOT NULL DEFAULT 'RUB',
    lot_size INTEGER NOT NULL DEFAULT 1 CHECK (lot_size > 0),
    last_price NUMERIC(20, 6),
    bid_price NUMERIC(20, 6),
    ask_price NUMERIC(20, 6),
    bid_volume BIGINT,
    ask_volume BIGINT,
    day_high NUMERIC(20, 6),
    day_low NUMERIC(20, 6),
    day_volume BIGINT,
    last_price_at TIMESTAMPTZ,
    quote_updated_at TIMESTAMPTZ,
    quote_received_at TIMESTAMPTZ,
    quote_source VARCHAR(30),
    snapshot_key VARCHAR(250)
);

INSERT INTO securities (id, ticker, company_name, exchange_code, currency_code, lot_size) VALUES
    (1, 'SBER',  'Сбербанк',                    'MOEX', 'RUB', 10),
    (2, 'SBERP', 'Сбербанк-п',                  'MOEX', 'RUB', 10),
    (3, 'GAZP',  'Газпром',                     'MOEX', 'RUB', 10),
    (4, 'LKOH',  'ЛУКОЙЛ',                      'MOEX', 'RUB', 1),
    (5, 'YDEX',  'Яндекс',                      'MOEX', 'RUB', 1),
    (6, 'ROSN',  'Роснефть',                    'MOEX', 'RUB', 1),
    (7, 'GMKN',  'Норильский никель',            'MOEX', 'RUB', 1),
    (8, 'NVTK',  'НОВАТЭК',                     'MOEX', 'RUB', 1),
    (9, 'TATN',  'Татнефть',                    'MOEX', 'RUB', 1),
    (10, 'VTBR', 'ВТБ',                         'MOEX', 'RUB', 10000),
    (11, 'MOEX', 'Московская биржа',             'MOEX', 'RUB', 10),
    (12, 'NLMK', 'НЛМК',                        'MOEX', 'RUB', 10),
    (13, 'CHMF', 'Северсталь',                  'MOEX', 'RUB', 1),
    (14, 'ALRS', 'АЛРОСА',                      'MOEX', 'RUB', 10),
    (15, 'AFLT', 'Аэрофлот',                    'MOEX', 'RUB', 10),
    (16, 'PLZL', 'Полюс',                       'MOEX', 'RUB', 1),
    (17, 'MTSS', 'МТС',                         'MOEX', 'RUB', 10),
    (18, 'MGNT', 'Магнит',                      'MOEX', 'RUB', 1),
    (19, 'SNGS', 'Сургутнефтегаз',              'MOEX', 'RUB', 100),
    (20, 'SNGSP','Сургутнефтегаз-п',             'MOEX', 'RUB', 100),
    (21, 'PHOR', 'ФосАгро',                     'MOEX', 'RUB', 1),
    (22, 'TRNFP','Транснефть-п',                'MOEX', 'RUB', 1),
    (23, 'BSPB', 'Банк Санкт-Петербург',          'MOEX', 'RUB', 1),
    (24, 'FIVE', 'X5',                          'MOEX', 'RUB', 1),
    (25, 'MAGN', 'ММК',                         'MOEX', 'RUB', 10),
    (26, 'RUAL', 'РУСАЛ',                       'MOEX', 'RUB', 10),
    (27, 'FESH', 'ДВМП',                        'MOEX', 'RUB', 1),
    (28, 'IRAO', 'Интер РАО',                   'MOEX', 'RUB', 100),
    (29, 'PIKK', 'ПИК',                         'MOEX', 'RUB', 1),
    (30, 'NMTP', 'НМТП',                        'MOEX', 'RUB', 10);
