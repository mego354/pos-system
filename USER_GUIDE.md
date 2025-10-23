# 📘 POS System - Complete User Guide

## Welcome to Your POS System! 🎉

This guide will help you use every feature of your Point of Sale system.

---

## 🚀 Starting the Application

1. Double-click `run-app.bat`
2. Wait for compilation (first time takes ~10 seconds)
3. The POS window will open automatically

---

## 🏠 Dashboard

The Dashboard is your home screen with business statistics.

### What You See:
- **Total Products**: Count of all products in inventory
- **Total Categories**: Number of product categories
- **Total Sales**: Number of completed transactions
- **Total Revenue**: Sum of all sales (in dollars)
- **Recent Sales Table**: Last 10 transactions with ID, Amount, and Date

### How to Use:
- Click **📊 Dashboard** in the sidebar anytime to return here
- Statistics update automatically when you make changes

---

## 📦 Product Management

Manage your inventory here.

### Viewing Products
- Click **📦 Products** in the sidebar
- See all products in a sortable table
- Columns: ID, Name, Category, Price, Stock, Image

### Adding a Product ➕
1. Click **➕ Add Product**
2. Fill in the form:
   - **Name**: Product name (required)
   - **Category**: Select from dropdown (required)
   - **Price**: Must be > 0
   - **Stock**: Must be >= 0
   - **Image**: Click "Browse..." to select an image file
3. Click **Save**

**Tip**: You can select PNG, JPG, JPEG, or GIF images!

### Editing a Product ✏️
1. Click on a product in the table to select it
2. Click **✏️ Edit**
3. Modify any fields
4. Click **Save**

### Deleting a Product 🗑️
1. Select a product from the table
2. Click **🗑️ Delete**
3. Confirm deletion
4. Product is removed from database

### Refreshing the View
- Click **🔄 Refresh** to reload the product list

---

## 📁 Category Management

Organize your products into categories.

### Viewing Categories
- Click **📁 Categories** in the sidebar
- See all categories in a table
- Columns: ID, Name, Description

### Adding a Category ➕
1. Click **➕ Add Category**
2. Enter:
   - **Name**: Category name (required)
   - **Description**: Optional details
3. Click **Save**

### Editing a Category ✏️
1. Select a category from the table
2. Click **✏️ Edit**
3. Update name or description
4. Click **Save**

### Deleting a Category 🗑️
1. Select a category
2. Click **🗑️ Delete**
3. Confirm deletion

**Important**: You cannot delete a category that has products assigned to it. First, reassign or delete those products.

---

## 🛒 Sales Module

Process customer transactions here.

### Sales Interface Layout

**Left Side - Product Catalog**:
- 🔍 Search bar: Type to find products by name
- 📁 Category filter: Select category to filter products
- Product table: Shows Name, Price, Stock
- "Add to Cart" buttons for each product

**Right Side - Shopping Cart**:
- Cart table: Shows Product, Quantity, Subtotal
- "✖" buttons to remove items
- **Total**: Shows cart total
- **💳 Process Sale**: Complete the transaction
- **Clear Cart**: Empty the cart

### Making a Sale - Step by Step

#### Step 1: Find Products
- **Search**: Type product name in search box
- **Filter**: Select category from dropdown
- Products appear in the table

#### Step 2: Add to Cart
- Click **Add to Cart** next to any product
- Product appears in the cart (right side)
- Click again to increase quantity
- Total updates automatically

**Automatic Checks**:
- ✅ Prevents adding out-of-stock items
- ✅ Prevents quantity exceeding available stock
- ✅ Shows warning messages

#### Step 3: Review Cart
- Check products in cart
- Verify quantities
- Remove unwanted items (click ✖)
- Check total amount

#### Step 4: Complete Sale
1. Click **💳 Process Sale**
2. System validates stock availability
3. Sale is recorded
4. Stock quantities are reduced
5. Success message shows Sale ID and Total
6. Cart clears automatically

### After the Sale
- View the sale in Dashboard > Recent Sales
- Stock levels are updated automatically
- Revenue statistics are updated

---

## 💡 Tips & Tricks

### Dashboard
- Use dashboard to quickly check business health
- Recent sales help track daily activity
- Stats update in real-time

### Products
- Upload clear product images for easy identification
- Keep stock quantities updated
- Use descriptive product names
- Organize products into categories for easier sales

### Categories
- Create categories before adding products
- Use clear, simple category names
- Add descriptions to help identify what goes where

### Sales
- Use search for quick product lookup
- Use category filter when browsing
- Double-check cart before processing
- Stock validates automatically - no overselling!

---

## ⚠️ Common Questions

### Q: What image formats are supported?
**A**: PNG, JPG, JPEG, and GIF files.

### Q: Can I delete a category with products?
**A**: No, you must first reassign or delete the products in that category.

### Q: What happens if I try to sell more than in stock?
**A**: The system will prevent this with a warning message.

### Q: Where is my data stored?
**A**: In `pos_system.db` file in the application folder.

### Q: Can I undo a sale?
**A**: Currently no, sales are final once processed. Future update may add this feature.

### Q: How do I backup my data?
**A**: Copy the `pos_system.db` file to a safe location.

---

## 🔧 Keyboard Shortcuts

- **Alt + D**: Dashboard (when window is focused)
- **Alt + P**: Products
- **Alt + C**: Categories
- **Alt + S**: Sales
- **Escape**: Close dialogs
- **Enter**: Confirm actions in dialogs

---

## 📊 Sample Data

Your system comes with sample data:

**Categories**:
- Electronics
- Clothing
- Books
- Food & Beverages
- Home & Garden

**Products**:
- Laptop ($999.99, 10 in stock)
- T-Shirt ($19.99, 50 in stock)
- Python Programming Book ($39.99, 30 in stock)
- Coffee Beans ($12.49, 100 in stock)
- Desk Lamp ($29.99, 25 in stock)
- Wireless Mouse ($24.99, 40 in stock)
- Water Bottle ($9.99, 75 in stock)

Feel free to edit or delete this sample data and add your own products!

---

## 🎯 Best Practices

1. **Daily**: Check Dashboard statistics
2. **Weekly**: Review and restock low inventory
3. **Monthly**: Analyze sales patterns
4. **Always**: Keep product info updated
5. **Regular**: Backup your database file

---

## 🆘 Need Help?

If something isn't working:
1. Check that all fields are filled correctly
2. Look for error messages
3. Try refreshing the view
4. Restart the application if needed
5. Check `README.md` for troubleshooting

---

## 🎊 Enjoy Your POS System!

You now have everything you need to run a successful retail operation. Happy selling! 🚀
