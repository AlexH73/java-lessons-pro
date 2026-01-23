DELETE
FROM cars;

INSERT INTO cars (id, brand, model, production_year, mileage, price,
                  status, color, horsepower, fuel_type, transmission)
VALUES (1,'Toyota', 'Camry', 2020, 35000, 18000,
        'AVAILABLE', 'Black', 200, 'PETROL', 'AUTOMATIC');

INSERT INTO cars (id, brand, model, production_year, mileage, price,
                  status, color, horsepower, fuel_type, transmission)
VALUES (2,'BMW', 'X5', 2018, 78000, 28000,
        'SOLD', 'White', 265, 'DIESEL', 'AUTOMATIC');