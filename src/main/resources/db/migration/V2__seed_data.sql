INSERT INTO hospital (id, name, lat, lng) VALUES
    (1, 'Beijing Central Hospital', 39.908692, 116.397477),
    (2, 'Shanghai People Hospital', 31.230416, 121.473701);

INSERT INTO product (id, name) VALUES
    (1, 'Cardiolex'),
    (2, 'Neurocare'),
    (3, 'Oncofine');

INSERT INTO hcp (id, name, hospital_id) VALUES
    (1, 'Dr. Wei Zhang', 1),
    (2, 'Dr. Lina Chen', 1),
    (3, 'Dr. Hao Liu', 2);

ALTER TABLE hospital ALTER COLUMN id RESTART WITH 100;
ALTER TABLE product ALTER COLUMN id RESTART WITH 100;
ALTER TABLE hcp ALTER COLUMN id RESTART WITH 100;
