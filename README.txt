# HealthFirst Pharmacy Inventory Management System

**Programming 732 **  
**Technology stack:** Java 21, Swing/AWT, JDBC, MySQL 8+  
**Author:** Katlego Sambo
**Student ITS number:** 402306874

## 1. Project overview

The **HealthFirst Pharmacy Inventory Management System (PIMS)** replaces manual stock records with a role-based desktop application. It is implemented as a Java Swing application and persists all operational data in a normalised MySQL database through JDBC. The program separates cashiers from administrators at sign-in. Cashiers can find medicines, inspect stock availability, create a cart, complete a sale, and print a bill. Administrators can manage medicines, suppliers, and users, and can view sales, item-wise sales, low-stock, and expiry reports.

The project uses MySQL Connector/J as the JDBC driver. The driver is bundled into the executable JAR by Maven, so the final JAR has no external Java library dependency. [1]

## 2. Delivered files

| Path | Purpose |
|---|---|
| `src/main/java/` | Complete Java source code, organised into `config`, `dao`, `model`, `ui`, and `util` packages. |
| `db.properties` and `src/main/resources/db.properties` | JDBC connection settings used by the application. The root file can be edited without rebuilding the JAR. |
| `database/pims_database.sql` | One complete MySQL script that creates the database and five tables, creates an application user, defines relationships, and inserts meaningful sample data. |
| `healthfirst-pims-1.0.0.jar` | Runnable fat JAR produced by Maven, containing the program and MySQL JDBC driver. |
| `run-pims.bat` / `run-pims.sh` | Windows and Linux launch scripts for the runnable JAR. |
| `package-windows-exe.bat` | Windows script that creates the native app image containing `YourName_pims.exe` using the JDK `jpackage` tool. |
| `screenshots/` | Actual application evidence screenshots prepared for the submission checklist. |
| `docs/` | Entity relationship diagram, implementation notes, and test evidence. |

## 3. Prerequisites

Install **Java Development Kit (JDK) 21 or later** to build the project. Java runtime access is sufficient to launch the supplied JAR. Install **MySQL Server 8+**, create a root/administrator account, and ensure that the MySQL service is running. Install Maven only when recompiling from source. Java's Swing toolkit is part of the standard desktop Java platform; no additional GUI library is required. [2]

## 4. Database installation

1. Open a terminal or MySQL command prompt in this project directory.
2. Run the complete script using a MySQL administrator account:

```bash
mysql -u root -p < database/pims_database.sql
```

3. The script creates the `healthfirst_pims` database and the restricted application account `pims_user` with password `pims_password`.
4. Do **not** run the script again after real data has been entered because its first statement removes and recreates the database. This design makes marking and demonstration setup reproducible.
5. If the local MySQL server uses different connection details, edit the root `db.properties` file before launching. The application checks this external file before using the bundled default configuration. Edit `src/main/resources/db.properties` only when changing the default for a future build.

The database uses InnoDB foreign keys and explicit primary keys. The `sales` / `sale_items` design is normalised because a sale header can contain many medicine line items without repeating sale-level information. Foreign keys prevent a supplier, user, or medicine from being removed when historical transactions require it.

## 5. Running the application

### Run the supplied JAR

After database installation, double-click `run-pims.bat` on Windows or execute the following command on Linux/macOS:

```bash
chmod +x run-pims.sh
./run-pims.sh
```

Alternatively, run the JAR directly:

```bash
java -jar healthfirst-pims-1.0.0.jar
```

### Build from source

```bash
mvn clean package
java -jar target/healthfirst-pims-1.0.0.jar
```

Maven compiles the program and the Shade plugin produces a single JAR with a `Main-Class` manifest entry. The application entry point is `com.healthfirst.pims.PimsApplication`.

### Build the requested Windows executable

A Windows `.exe` must be generated on Windows because `jpackage` creates native packages for the operating system where it runs. Copy this project to a Windows computer with JDK 21 and Maven. Double-click `package-windows-exe.bat`. The generated native executable will be placed at `dist/YourName_pims/YourName_pims.exe`, with its required Java runtime in the same app-image folder. Rename `YourName` to the student’s name before final submission. [3]

## 6. Default login accounts

| Role | Username | Password | Available functions |
|---|---|---|---|
| Administrator | `admin` | `admin123` | Dashboard, medicine CRUD, supplier CRUD, user CRUD, and all reports. |
| Administrator | `manager` | `manager123` | Same administrator permissions; supplied to demonstrate multiple-admin support. |
| Cashier | `cashier` | `cash123` | Point of Sale, stock check, cart controls, checkout, bill generation, and printing. |

Passwords are stored as SHA-256 hashes rather than plaintext. The demonstration passwords are deliberately documented above to allow markers to test the system. In a production system, password hashes should also be salted with an adaptive password hashing scheme.

## 7. Functional user guide

### Cashier workflow

A cashier signs in using the `cashier` account. The Point of Sale screen lists available medicines. Enter part of a medicine name, company, or medicine type in the search box to filter the list. Select a row and select **Check stock** to view current quantity, price, and expiry date without changing inventory. Choose a quantity and select **Add to cart** to create or extend a cart line. The program prevents the cart quantity from exceeding available stock.

Select a cart row to add one unit, remove one unit, or delete the line. Select **Checkout and generate bill** to complete the sale. Checkout is transactional: the sale header, every `sale_items` record, and every stock deduction succeed together or the application rolls back all work. A printable bill window appears after a successful sale. Cashiers cannot see administrative tabs.

### Administrator workflow

An administrator signs in using `admin`. The Dashboard displays product, low-stock, and thirty-day expiry indicators. **Manage Medicines** provides Create, Read, Update, and Delete operations for the core stock catalogue. Add suppliers before adding a medicine because each medicine must reference a valid supplier.

**Manage Suppliers** maintains supplier contact details. **Manage Users** creates cashier or administrator accounts, changes usernames, names, roles, and passwords, and deletes accounts. A blank password during an update intentionally retains the existing password. The signed-in administrator cannot delete their own account.

The **Reports** tab contains four reports. Sales and item-wise reports accept an inclusive date range. The low-stock report selects medicines at or below their reorder level. The expiry report selects medicines expiring between today and a date chosen by the administrator. The seeded data deliberately includes low-stock and soon-to-expire medicines so that these reports produce results immediately.

## 8. Design and data integrity decisions

| Requirement | Implementation decision | Benefit |
|---|---|---|
| Role-based access | The authenticated `User` record exposes `isAdmin()`. The dashboard constructs only the correct workspace for the role. | A cashier cannot navigate to user, stock maintenance, or report controls. |
| Secure database access | Every DAO query uses `PreparedStatement` placeholders. | User input is parameterised rather than concatenated into SQL. |
| Monetary values | Prices and totals are represented with `BigDecimal` and `DECIMAL(10,2)`. | Currency calculations remain exact. |
| Stock accuracy | `SaleDao.checkout()` obtains row locks with `FOR UPDATE`, checks availability, inserts sales, inserts items, deducts stock, and commits as one transaction. | Concurrent cashiers cannot oversell a medicine during normal transactional operation. |
| Auditability | The `sale_items.price_at_sale` field records the charged price, independent of future catalogue price edits. | Historical reports and receipts remain correct. |
| Referential integrity | Foreign keys use `RESTRICT` where historical data must be retained and `CASCADE` only from a sale header to its own line items. | Invalid orphaned data is prevented. |
| Input validation | GUI controls reject blank required fields, invalid dates, negative values, empty carts, and actions without a selected row. | Common data-entry errors are handled before a database operation. |

## 9. Marking demonstration checklist

| Assignment requirement | Demonstration action |
|---|---|
| Secure login and role redirection | Log in as `admin`, log out, then log in as `cashier`; compare the available workspace. |
| Medicine full CRUD | Add a test medicine, edit its stock level, and delete it if it has no sales. |
| Supplier CRUD | Add a supplier, update its contact person, and delete it only after no medicine references it. |
| User CRUD | Create a test cashier, sign in with it, then remove it from the administrator account. |
| POS, billing, cart clearing | Add two medicines to the cashier cart, adjust quantities, checkout, print the bill, and show the cleared cart. |
| Stock check | Select a medicine and click **Check stock** without adding it to the cart. |
| Sales report | Use a date range containing the seeded sales from the previous five days. |
| Item-wise report | Select **Item-wise report** for the same date range. |
| Low-stock report | Select **Low-stock report** to show Benylin and Efferflu. |
| Expiry report | Leave the default one-month expiry end date and select **Expiry report**. |

## 10. Troubleshooting

| Symptom | Likely cause and resolution |
|---|---|
| `Cannot connect to MySQL` on login | Start MySQL, then execute `database/pims_database.sql`. Confirm that `db.properties` has the correct host, port, user, and password. |
| `Access denied for user pims_user` | Execute the SQL script as a MySQL administrator. It creates and grants the restricted database account. |
| `java is not recognized` | Install JDK 21+ and add its `bin` directory to the system `PATH`. |
| No report data | Use a date range that includes the seeded sales, or make a cashier sale first. |
| Cannot delete a medicine/user/supplier | The database correctly protects records that are referenced by sale history or medicine records. Use an unused test record instead. |

## 11. Submission preparation

Replace the placeholders at the top of this document. Create the native `.exe` using the included Windows script if the lecturer strictly requires an `.exe`. Place the final `.exe`, the complete `src` folder, `database/pims_database.sql`, the populated `screenshots` folder, and this README in a folder named with the required convention. Compress it as `YourName_StudentNumber_PRO732.zip`. The complete archive must remain under 50 MB.

## References

[1]: https://dev.mysql.com/doc/connector-j/en/ "MySQL Connector/J Developer Guide"
[2]: https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/javax/swing/package-summary.html "Java SE 21 Swing Package Documentation"
[3]: https://docs.oracle.com/en/java/javase/21/jpackage/packaging-tool-user-guide.pdf "Java Packaging Tool User Guide"
