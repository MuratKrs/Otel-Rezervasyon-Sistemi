--
-- PostgreSQL database dump
--

\restrict Lm7EEfqMxcQe5nvBvhQopBk3yOi2dddcWoaatExIusMDONpPloUrUDrmokAliEo

-- Dumped from database version 17.6
-- Dumped by pg_dump version 17.6

-- Started on 2025-12-20 21:54:50

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
-- TOC entry 232 (class 1255 OID 16551)
-- Name: calculate_total_price(integer, date, date); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.calculate_total_price(p_room_id integer, p_start_date date, p_end_date date) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE
    v_price DECIMAL(10, 2);
    v_days INT;
BEGIN
    -- Odanın gecelik fiyatını al
    SELECT price_per_night INTO v_price FROM rooms WHERE room_id = p_room_id;
    
    -- Gün farkını bul
    v_days := p_end_date - p_start_date;
    
    -- Toplam fiyatı döndür (Eğer gün 0 veya negatifse hata kontrolü eklenebilir)
    RETURN v_price * v_days;
END;
$$;


ALTER FUNCTION public.calculate_total_price(p_room_id integer, p_start_date date, p_end_date date) OWNER TO postgres;

--
-- TOC entry 234 (class 1255 OID 16654)
-- Name: log_reservation_cancel_func(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.log_reservation_cancel_func() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- 1. Silinen rezervasyonu Log tablosuna kaydet
    INSERT INTO audit_logs (reservation_id, action_type, description)
    VALUES (OLD.reservation_id, 'DELETE', 'Rezervasyon iptal edildi. Oda ID: ' || OLD.room_id);
    
    -- 2. KRİTİK KISIM: Odayı SİLME, sadece durumunu GÜNCELLE
    UPDATE rooms SET status = 'AVAILABLE' WHERE room_id = OLD.room_id;
    
    RETURN OLD;
END;
$$;


ALTER FUNCTION public.log_reservation_cancel_func() OWNER TO postgres;

--
-- TOC entry 233 (class 1255 OID 16552)
-- Name: update_room_status_func(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_room_status_func() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE rooms 
    SET status = 'OCCUPIED' 
    WHERE room_id = NEW.room_id;
    
    -- Ayrıca toplam fiyatı otomatik hesaplayıp tabloya yazalım
    NEW.total_price := calculate_total_price(NEW.room_id, NEW.start_date, NEW.end_date);
    
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.update_room_status_func() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 224 (class 1259 OID 16542)
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_logs (
    log_id integer NOT NULL,
    reservation_id integer,
    action_type character varying(50),
    description text,
    log_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.audit_logs OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16541)
-- Name: audit_logs_log_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.audit_logs_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_logs_log_id_seq OWNER TO postgres;

--
-- TOC entry 4990 (class 0 OID 0)
-- Dependencies: 223
-- Name: audit_logs_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.audit_logs_log_id_seq OWNED BY public.audit_logs.log_id;


--
-- TOC entry 228 (class 1259 OID 16574)
-- Name: features; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.features (
    feature_id integer NOT NULL,
    feature_name character varying(50) NOT NULL
);


ALTER TABLE public.features OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16573)
-- Name: features_feature_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.features_feature_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.features_feature_id_seq OWNER TO postgres;

--
-- TOC entry 4991 (class 0 OID 0)
-- Dependencies: 227
-- Name: features_feature_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.features_feature_id_seq OWNED BY public.features.feature_id;


--
-- TOC entry 231 (class 1259 OID 16598)
-- Name: payments; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.payments (
    payment_id integer NOT NULL,
    reservation_id integer,
    amount numeric(10,2) NOT NULL,
    payment_date timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    payment_method character varying(50)
);


ALTER TABLE public.payments OWNER TO postgres;

--
-- TOC entry 230 (class 1259 OID 16597)
-- Name: payments_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.payments_payment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.payments_payment_id_seq OWNER TO postgres;

--
-- TOC entry 4992 (class 0 OID 0)
-- Dependencies: 230
-- Name: payments_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.payments_payment_id_seq OWNED BY public.payments.payment_id;


--
-- TOC entry 222 (class 1259 OID 16524)
-- Name: reservations; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.reservations (
    reservation_id integer NOT NULL,
    user_id integer,
    room_id integer,
    start_date date NOT NULL,
    end_date date NOT NULL,
    total_price numeric(10,2),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.reservations OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 16523)
-- Name: reservations_reservation_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.reservations_reservation_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.reservations_reservation_id_seq OWNER TO postgres;

--
-- TOC entry 4993 (class 0 OID 0)
-- Dependencies: 221
-- Name: reservations_reservation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.reservations_reservation_id_seq OWNED BY public.reservations.reservation_id;


--
-- TOC entry 229 (class 1259 OID 16582)
-- Name: room_features; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.room_features (
    room_id integer NOT NULL,
    feature_id integer NOT NULL
);


ALTER TABLE public.room_features OWNER TO postgres;

--
-- TOC entry 226 (class 1259 OID 16558)
-- Name: room_types; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.room_types (
    type_id integer NOT NULL,
    type_name character varying(50) NOT NULL,
    description text
);


ALTER TABLE public.room_types OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16557)
-- Name: room_types_type_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.room_types_type_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.room_types_type_id_seq OWNER TO postgres;

--
-- TOC entry 4994 (class 0 OID 0)
-- Dependencies: 225
-- Name: room_types_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.room_types_type_id_seq OWNED BY public.room_types.type_id;


--
-- TOC entry 220 (class 1259 OID 16511)
-- Name: rooms; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.rooms (
    room_id integer NOT NULL,
    room_number character varying(10) NOT NULL,
    price_per_night numeric(10,2) NOT NULL,
    status character varying(20) DEFAULT 'AVAILABLE'::character varying,
    type_id integer,
    CONSTRAINT rooms_status_check CHECK (((status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'OCCUPIED'::character varying])::text[])))
);


ALTER TABLE public.rooms OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 16510)
-- Name: rooms_room_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.rooms_room_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.rooms_room_id_seq OWNER TO postgres;

--
-- TOC entry 4995 (class 0 OID 0)
-- Dependencies: 219
-- Name: rooms_room_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.rooms_room_id_seq OWNED BY public.rooms.room_id;


--
-- TOC entry 218 (class 1259 OID 16500)
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    user_id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(50) NOT NULL,
    full_name character varying(100) NOT NULL,
    role character varying(20),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT users_role_check CHECK (((role)::text = ANY ((ARRAY['ADMIN'::character varying, 'CUSTOMER'::character varying])::text[])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- TOC entry 217 (class 1259 OID 16499)
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_user_id_seq OWNER TO postgres;

--
-- TOC entry 4996 (class 0 OID 0)
-- Dependencies: 217
-- Name: users_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_user_id_seq OWNED BY public.users.user_id;


--
-- TOC entry 4785 (class 2604 OID 16545)
-- Name: audit_logs log_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs ALTER COLUMN log_id SET DEFAULT nextval('public.audit_logs_log_id_seq'::regclass);


--
-- TOC entry 4788 (class 2604 OID 16577)
-- Name: features feature_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.features ALTER COLUMN feature_id SET DEFAULT nextval('public.features_feature_id_seq'::regclass);


--
-- TOC entry 4789 (class 2604 OID 16601)
-- Name: payments payment_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments ALTER COLUMN payment_id SET DEFAULT nextval('public.payments_payment_id_seq'::regclass);


--
-- TOC entry 4783 (class 2604 OID 16527)
-- Name: reservations reservation_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservations ALTER COLUMN reservation_id SET DEFAULT nextval('public.reservations_reservation_id_seq'::regclass);


--
-- TOC entry 4787 (class 2604 OID 16561)
-- Name: room_types type_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.room_types ALTER COLUMN type_id SET DEFAULT nextval('public.room_types_type_id_seq'::regclass);


--
-- TOC entry 4781 (class 2604 OID 16514)
-- Name: rooms room_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rooms ALTER COLUMN room_id SET DEFAULT nextval('public.rooms_room_id_seq'::regclass);


--
-- TOC entry 4779 (class 2604 OID 16503)
-- Name: users user_id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN user_id SET DEFAULT nextval('public.users_user_id_seq'::regclass);


--
-- TOC entry 4977 (class 0 OID 16542)
-- Dependencies: 224
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.audit_logs (log_id, reservation_id, action_type, description, log_date) FROM stdin;
1	1	DELETE	Rezervasyon iptal edildi. Oda ID: 4	2025-12-03 19:17:02.842738
2	2	DELETE	Rezervasyon iptal edildi. Oda ID: 4	2025-12-03 19:48:38.211078
3	4	DELETE	Rezervasyon iptal edildi. Oda ID: 7	2025-12-03 20:09:19.896121
4	3	DELETE	Rezervasyon iptal edildi. Oda ID: 6	2025-12-03 20:40:55.127928
5	6	DELETE	Rezervasyon iptal edildi. Oda ID: 5	2025-12-03 21:05:50.340941
6	5	DELETE	Rezervasyon iptal edildi. Oda ID: 7	2025-12-03 21:06:21.625114
7	7	DELETE	Rezervasyon iptal edildi. Oda ID: 1	2025-12-20 18:33:44.371646
\.


--
-- TOC entry 4981 (class 0 OID 16574)
-- Dependencies: 228
-- Data for Name: features; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.features (feature_id, feature_name) FROM stdin;
8	Klima
9	TV
10	Ücretsiz Wifi
11	Minibar
12	Jakuzi
13	Kasa
14	Deniz Manzarası
\.


--
-- TOC entry 4984 (class 0 OID 16598)
-- Dependencies: 231
-- Data for Name: payments; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.payments (payment_id, reservation_id, amount, payment_date, payment_method) FROM stdin;
4	8	15000.00	2025-12-20 18:30:43.077371	Kredi Kartı
\.


--
-- TOC entry 4975 (class 0 OID 16524)
-- Dependencies: 222
-- Data for Name: reservations; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.reservations (reservation_id, user_id, room_id, start_date, end_date, total_price, created_at) FROM stdin;
8	2	2	2025-12-20	2025-12-30	15000.00	2025-12-20 18:30:43.077371
\.


--
-- TOC entry 4982 (class 0 OID 16582)
-- Dependencies: 229
-- Data for Name: room_features; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.room_features (room_id, feature_id) FROM stdin;
1	8
1	9
1	10
2	8
2	9
2	10
3	8
3	9
3	10
4	8
4	9
4	10
5	8
5	9
5	10
6	8
6	9
6	10
7	8
7	9
7	10
8	8
8	9
8	10
9	8
9	9
9	10
10	8
10	9
10	10
11	8
11	9
11	10
12	8
12	9
12	10
13	8
13	9
13	10
14	8
14	9
14	10
15	8
15	9
15	10
16	8
16	9
16	10
17	8
17	9
17	10
18	8
18	9
18	10
19	8
19	9
19	10
20	8
20	9
20	10
21	8
21	9
21	10
21	11
21	12
22	8
22	9
22	10
22	11
22	12
23	8
23	9
23	10
23	11
23	12
24	8
24	9
24	10
24	11
24	12
25	8
25	9
25	10
25	11
25	12
26	8
26	9
26	10
26	11
26	12
27	8
27	9
27	10
27	11
27	12
28	8
28	9
28	10
28	11
28	12
29	8
29	9
29	10
29	11
29	12
30	8
30	9
30	10
30	11
30	12
31	8
31	9
31	10
31	11
31	12
31	13
31	14
32	8
32	9
32	10
32	11
32	12
32	13
32	14
33	8
33	9
33	10
33	11
33	12
33	13
33	14
34	8
34	9
34	10
34	11
34	12
34	13
34	14
35	8
35	9
35	10
35	11
35	12
35	13
35	14
36	8
36	9
36	10
36	11
36	12
36	13
36	14
37	8
37	9
37	10
37	11
37	12
37	13
37	14
38	8
38	9
38	10
38	11
38	12
38	13
38	14
39	8
39	9
39	10
39	11
39	12
39	13
39	14
40	8
40	9
40	10
40	11
40	12
40	13
40	14
\.


--
-- TOC entry 4979 (class 0 OID 16558)
-- Dependencies: 226
-- Data for Name: room_types; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.room_types (type_id, type_name, description) FROM stdin;
1	Tek Kişilik	Standart tek yataklı oda
2	Çift Kişilik	Geniş yataklı veya iki ayrı yataklı oda
3	Suit	Oturma alanı olan geniş oda
4	Kral Dairesi	En üst düzey lüks oda
\.


--
-- TOC entry 4973 (class 0 OID 16511)
-- Dependencies: 220
-- Data for Name: rooms; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.rooms (room_id, room_number, price_per_night, status, type_id) FROM stdin;
3	12	1500.00	AVAILABLE	1
4	13	1500.00	AVAILABLE	1
6	15	1500.00	AVAILABLE	1
7	16	1500.00	AVAILABLE	1
8	17	1500.00	AVAILABLE	1
9	18	1500.00	AVAILABLE	1
10	19	1500.00	AVAILABLE	1
11	20	2500.00	AVAILABLE	2
12	21	2500.00	AVAILABLE	2
13	22	2500.00	AVAILABLE	2
14	23	2500.00	AVAILABLE	2
15	24	2500.00	AVAILABLE	2
16	25	2500.00	AVAILABLE	2
17	26	2500.00	AVAILABLE	2
18	27	2500.00	AVAILABLE	2
19	28	2500.00	AVAILABLE	2
20	29	2500.00	AVAILABLE	2
21	30	5000.00	AVAILABLE	3
22	31	5000.00	AVAILABLE	3
23	32	5000.00	AVAILABLE	3
24	33	5000.00	AVAILABLE	3
25	34	5000.00	AVAILABLE	3
26	35	5000.00	AVAILABLE	3
27	36	5000.00	AVAILABLE	3
28	37	5000.00	AVAILABLE	3
29	38	5000.00	AVAILABLE	3
30	39	5000.00	AVAILABLE	3
31	40	12000.00	AVAILABLE	4
32	41	12000.00	AVAILABLE	4
33	42	12000.00	AVAILABLE	4
34	43	12000.00	AVAILABLE	4
35	44	12000.00	AVAILABLE	4
36	45	12000.00	AVAILABLE	4
37	46	12000.00	AVAILABLE	4
38	47	12000.00	AVAILABLE	4
39	48	12000.00	AVAILABLE	4
40	49	12000.00	AVAILABLE	4
5	14	2000.00	AVAILABLE	1
2	11	1500.00	OCCUPIED	1
1	10	1500.00	AVAILABLE	1
\.


--
-- TOC entry 4971 (class 0 OID 16500)
-- Dependencies: 218
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, username, password, full_name, role, created_at) FROM stdin;
1	admin	1234	Sistem Yöneticisi	ADMIN	2025-12-17 00:25:02.980885
2	ali	1234	ali veli	CUSTOMER	2025-12-20 18:28:37.444335
\.


--
-- TOC entry 4997 (class 0 OID 0)
-- Dependencies: 223
-- Name: audit_logs_log_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.audit_logs_log_id_seq', 7, true);


--
-- TOC entry 4998 (class 0 OID 0)
-- Dependencies: 227
-- Name: features_feature_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.features_feature_id_seq', 14, true);


--
-- TOC entry 4999 (class 0 OID 0)
-- Dependencies: 230
-- Name: payments_payment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.payments_payment_id_seq', 4, true);


--
-- TOC entry 5000 (class 0 OID 0)
-- Dependencies: 221
-- Name: reservations_reservation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.reservations_reservation_id_seq', 8, true);


--
-- TOC entry 5001 (class 0 OID 0)
-- Dependencies: 225
-- Name: room_types_type_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.room_types_type_id_seq', 4, true);


--
-- TOC entry 5002 (class 0 OID 0)
-- Dependencies: 219
-- Name: rooms_room_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.rooms_room_id_seq', 41, true);


--
-- TOC entry 5003 (class 0 OID 0)
-- Dependencies: 217
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 3, true);


--
-- TOC entry 4804 (class 2606 OID 16550)
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (log_id);


--
-- TOC entry 4810 (class 2606 OID 16581)
-- Name: features features_feature_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.features
    ADD CONSTRAINT features_feature_name_key UNIQUE (feature_name);


--
-- TOC entry 4812 (class 2606 OID 16579)
-- Name: features features_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.features
    ADD CONSTRAINT features_pkey PRIMARY KEY (feature_id);


--
-- TOC entry 4816 (class 2606 OID 16604)
-- Name: payments payments_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_pkey PRIMARY KEY (payment_id);


--
-- TOC entry 4802 (class 2606 OID 16530)
-- Name: reservations reservations_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservations
    ADD CONSTRAINT reservations_pkey PRIMARY KEY (reservation_id);


--
-- TOC entry 4814 (class 2606 OID 16586)
-- Name: room_features room_features_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.room_features
    ADD CONSTRAINT room_features_pkey PRIMARY KEY (room_id, feature_id);


--
-- TOC entry 4806 (class 2606 OID 16565)
-- Name: room_types room_types_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.room_types
    ADD CONSTRAINT room_types_pkey PRIMARY KEY (type_id);


--
-- TOC entry 4808 (class 2606 OID 16567)
-- Name: room_types room_types_type_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.room_types
    ADD CONSTRAINT room_types_type_name_key UNIQUE (type_name);


--
-- TOC entry 4798 (class 2606 OID 16520)
-- Name: rooms rooms_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT rooms_pkey PRIMARY KEY (room_id);


--
-- TOC entry 4800 (class 2606 OID 16522)
-- Name: rooms rooms_room_number_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT rooms_room_number_key UNIQUE (room_number);


--
-- TOC entry 4794 (class 2606 OID 16507)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- TOC entry 4796 (class 2606 OID 16509)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 4823 (class 2620 OID 16655)
-- Name: reservations trg_audit_cancel; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_audit_cancel AFTER DELETE ON public.reservations FOR EACH ROW EXECUTE FUNCTION public.log_reservation_cancel_func();


--
-- TOC entry 4824 (class 2620 OID 16553)
-- Name: reservations trg_update_room_status; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trg_update_room_status BEFORE INSERT ON public.reservations FOR EACH ROW EXECUTE FUNCTION public.update_room_status_func();


--
-- TOC entry 4817 (class 2606 OID 16568)
-- Name: rooms fk_room_type; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.rooms
    ADD CONSTRAINT fk_room_type FOREIGN KEY (type_id) REFERENCES public.room_types(type_id);


--
-- TOC entry 4822 (class 2606 OID 16658)
-- Name: payments payments_reservation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.payments
    ADD CONSTRAINT payments_reservation_id_fkey FOREIGN KEY (reservation_id) REFERENCES public.reservations(reservation_id) ON DELETE CASCADE;


--
-- TOC entry 4818 (class 2606 OID 16536)
-- Name: reservations reservations_room_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservations
    ADD CONSTRAINT reservations_room_id_fkey FOREIGN KEY (room_id) REFERENCES public.rooms(room_id);


--
-- TOC entry 4819 (class 2606 OID 16531)
-- Name: reservations reservations_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.reservations
    ADD CONSTRAINT reservations_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id);


--
-- TOC entry 4820 (class 2606 OID 16592)
-- Name: room_features room_features_feature_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.room_features
    ADD CONSTRAINT room_features_feature_id_fkey FOREIGN KEY (feature_id) REFERENCES public.features(feature_id) ON DELETE CASCADE;


--
-- TOC entry 4821 (class 2606 OID 16587)
-- Name: room_features room_features_room_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.room_features
    ADD CONSTRAINT room_features_room_id_fkey FOREIGN KEY (room_id) REFERENCES public.rooms(room_id) ON DELETE CASCADE;


-- Completed on 2025-12-20 21:54:50

--
-- PostgreSQL database dump complete
--

\unrestrict Lm7EEfqMxcQe5nvBvhQopBk3yOi2dddcWoaatExIusMDONpPloUrUDrmokAliEo

