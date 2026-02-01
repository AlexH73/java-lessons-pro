-- Test Cars
INSERT INTO cars (id, brand, model, production_year, mileage, price, status, color, horsepower, fuel_type, transmission,
                  created_at, updated_at, deleted)
VALUES (1, 'BMW', 'X5', 2023, 10000, 75000.00, 'AVAILABLE', 'Black', 250, 'PETROL', 'AUTOMATIC', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, false),
       (2, 'Audi', 'A6', 2022, 15000, 55000.00, 'AVAILABLE', 'White', 200, 'DIESEL', 'AUTOMATIC', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, false),
       (3, 'Mercedes', 'E-Class', 2023, 5000, 80000.00, 'AVAILABLE', 'Silver', 300, 'HYBRID', 'AUTOMATIC',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false);

-- Test Test Drive Bookings
INSERT INTO test_drive_bookings (id, client_email, client_name, car_id, car_brand, car_model, car_year, car_color,
                                 car_horsepower, car_transmission, car_fuel_type, car_mileage, car_price,
                                 test_drive_date_time, dealer_address, dealer_phone, confirmation_id, reminder_id,
                                 reminder_sent, created_at, status)
VALUES (1, 'test1@example.com', 'Иван Иванов', 1, 'BMW', 'X5', 2023, 'Black', 250, 'AUTOMATIC', 'PETROL', 10000,
        '75000.00 €', DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'ул. Тестовая, 1', '+7 (999) 123-45-67', 'TD-TEST001', NULL,
        false, CURRENT_TIMESTAMP, 'CONFIRMED'),
       (2, 'test2@example.com', 'Петр Петров', 2, 'Audi', 'A6', 2022, 'White', 200, 'AUTOMATIC', 'DIESEL', 15000,
        '55000.00 €', DATEADD('DAY', 2, CURRENT_TIMESTAMP), 'ул. Тестовая, 1', '+7 (999) 123-45-67', 'TD-TEST002',
        'REM-TEST002', true, CURRENT_TIMESTAMP, 'REMINDER_SENT');