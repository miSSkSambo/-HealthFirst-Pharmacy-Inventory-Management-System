# HealthFirst PIMS: Implementation and Test Evidence

**Module:** Programming 732  
**Project:** Pharmacy Inventory Management System  
**Student:** _Replace with Name and Surname_  
**ITS Number:** _Replace with Student ITS Number_

## Introduction

This report describes the design and implementation of the HealthFirst Pharmacy Inventory Management System. The solution is a multi-user desktop application implemented in Java Swing and connected to MySQL through JDBC. It addresses the required pharmacy operations of medicine inventory maintenance, cashier sales processing, billing, supplier management, user management, and management reporting. [4]

## System architecture

The source code uses a layered structure. The `ui` package contains Swing windows and panels. The `dao` package contains database-specific code. The `model` package contains immutable data records and the mutable point-of-sale cart line. The `config` package creates JDBC connections, and the `util` package centralises password hashing and presentation formats. This separation means that display code does not contain embedded SQL, and database code does not need to know how a screen is arranged.

| Layer | Principal classes | Responsibility |
|---|---|---|
| Entry | `PimsApplication` | Starts Swing on the Event Dispatch Thread and opens the login window. |
| Presentation | `LoginFrame`, `DashboardFrame`, `CashierPanel`, management panels, `ReportsPanel`, `ReceiptDialog` | Presents role-specific workflows, validates user input, and shows feedback. |
| Data access | `UserDao`, `SupplierDao`, `MedicineDao`, `SaleDao`, `ReportDao` | Performs parameterised CRUD, transaction, search, and report queries. |
| Domain | `User`, `Supplier`, `Medicine`, `CartItem`, `SaleResult`, `ReportRow` | Represents application data passed between the presentation and data layers. |
| Configuration and utilities | `Database`, `PasswordUtil`, `FormatUtil` | Loads JDBC properties, hashes passwords, and formats dates/currency. |

## Database design

The schema contains five related tables. `users` stores sign-in identities and roles. `suppliers` stores supplier contacts. `medicines` stores product and inventory values and holds a foreign key to suppliers. `sales` is the transaction header and holds a foreign key to the cashier. `sale_items` represents the one-to-many child records of each sale and records the quantity and price charged for each medicine.

The design is normalised. A sale may contain many medicines, so medicine quantities are not stored as repeated columns in `sales`. Instead, each line is stored in `sale_items`. This permits item-wise reporting and preserves an exact historical price. The full schema with data types, primary keys, foreign keys, validation constraints, indexes, and sample inserts is in `database/pims_database.sql`.

## Security and transaction controls

Login uses a username and SHA-256 password hash comparison. The source never sends a plaintext password to the database. All SQL uses JDBC `PreparedStatement` parameters. These controls reduce accidental query breakage and avoid directly concatenating entered values into SQL statements.

The checkout process is the most critical data operation. It starts a JDBC transaction and locks each medicine row. It checks stock availability while the row is locked, creates the sale header, inserts every sale item, and decrements each medicine quantity. The transaction commits only after all database statements succeed. If an exception occurs, the connection rolls back. Therefore a partial bill or an unrecorded stock deduction is not left in the database.

## Test cases and expected results

| ID | Test procedure | Expected result | Result |
|---|---|---|---|
| T01 | Start the application after importing the SQL script. | The login screen opens and shows the two primary demo credentials. | Pass |
| T02 | Sign in as `admin` / `admin123`. | The administrator dashboard with five tabs opens. | Pass |
| T03 | Sign in as `cashier` / `cash123`. | The Point of Sale workspace opens; no administration tabs are displayed. | Pass |
| T04 | Add a unique supplier through Manage Suppliers. | The supplier appears in the table. | Pass |
| T05 | Add, edit, and remove an unused medicine. | CRUD changes are written to MySQL and displayed after reload. | Pass |
| T06 | Select a product and run Check stock. | A dialog displays current quantity, price, and expiry information. | Pass |
| T07 | Add more units than are in stock. | The cart rejects the request and displays the available quantity. | Pass |
| T08 | Complete a sale with two products. | A sale header and item rows are created, stock is reduced, the cart clears, and a bill window opens. | Pass |
| T09 | View the four reports after import. | Sales, item-wise, low-stock, and expiry tables return the seeded data. | Pass |
| T10 | Attempt to delete a medicine referenced by a sale. | MySQL rejects the deletion, preserving historical report data. | Pass |

## Conclusion

The HealthFirst PIMS solution meets the stated functional requirements with an accessible Swing interface, a MySQL relational schema, role separation, full administrative CRUD, a transactional cashier Point of Sale process, a printable bill, and four management reports. The project is supplied with seeded test data, runnable build artefacts, user instructions, screenshots, and a Windows packaging script so that a marker can install and test it consistently.

## References

[1]: https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Connection.html "Java SE 21 Connection Interface Documentation"
[2]: https://dev.mysql.com/doc/refman/8.4/en/innodb-transaction-model.html "MySQL InnoDB Transaction Model"
[3]: https://dev.mysql.com/doc/connector-j/en/connector-j-usagenotes-basic.html "MySQL Connector/J Basic Usage"
[4]: ../PROGRAMMING732ASSIGNMENT.pdf "Programming 732 Assignment Brief (provided)"
