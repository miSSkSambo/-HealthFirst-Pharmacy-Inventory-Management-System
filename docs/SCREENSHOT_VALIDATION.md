# Screenshot Validation Record

The screenshots in this folder were captured from the compiled HealthFirst PIMS JAR in a 1280 × 800 virtual desktop after importing `database/pims_database.sql` into a MySQL-compatible test server. The evidence set covers the explicitly requested interface images. [1]

| Screenshot | Validation observation |
|---|---|
| `01-login-screen.png` | Login screen is centred and presents username, password, sign-in, and the documented demo credentials. |
| `02-admin-dashboard.png` | Successful administrator login is shown. The screenshot contains the Dashboard, Manage Medicines, Manage Suppliers, Manage Users, and Reports tabs, along with current stock summary cards. |
| `03-manage-medicines.png` | Captures the administrator medicine-management interface with the seeded catalogue and full CRUD form controls. |
| `04-sales-report.png` | Captures the date-filtered sales summary report. |
| `05-item-wise-sales-report.png` | Captures item-level sales performance report. |
| `06-low-stock-report.png` | Captures medicines at or below reorder level. |
| `07-expiry-report.png` | Captures medicines expiring within the configured period. |
| `08-cashier-pos-with-cart.png` | Captures a cashier Point of Sale workspace with a populated cart. |
| `09-generated-bill.png` | Captures the printable bill dialog generated from a completed sale. |

The screenshots can be used directly for the assignment submission checklist. The setup and capture procedure is preserved in `docs/capture_screenshots.sh` for reproducibility.

## Review update

The login, administrator dashboard, and medicine-management screenshots were visually reviewed and show the expected controls and seeded content. Initial review found that the first captured cashier image did not yet contain the intended cart item because the scripted Add to Cart coordinate was above the actual action button. The capture script was corrected and the cashier and receipt evidence screens were recaptured before final packaging. The final cashier screenshot displays one Allergex 4mg cart line and a R58,75 total. The final receipt screenshot displays Bill 4, cashier Thabo Nkosi, the charged item, the total, and Print bill / Close controls.

Initial report captures also revealed that the Reports tab coordinate selected Manage Users. The Reports tab coordinate was corrected and all report screenshots were regenerated from the report interface before final packaging.

The recaptured Sales report screenshot was visually validated and shows a populated date-filtered sales summary. The first placement of subsequent report action coordinates did not map to the intended Item-wise, Low-stock, and Expiry controls; those action coordinates were corrected and the three screenshots were regenerated before final packaging. Final review confirms that `05-item-wise-sales-report.png` contains medicine performance rows and `06-low-stock-report.png` contains Efferflu C, Benylin Cough Syrup, and Betadine Antiseptic at or below the configured reorder level.

## References

[1]: ../PROGRAMMING732ASSIGNMENT.pdf "Programming 732 Assignment Brief (provided)"
