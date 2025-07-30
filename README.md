# Skincare App - API Comparison Project

An Android application that demonstrates **dual API implementation** (REST and GraphQL) for fetching skincare products from a PostgreSQL database, designed for **fair performance comparison**.

---

## 🚀 Features

### **Core Features**
- **Dual API Support**: REST (Retrofit) vs GraphQL (Apollo) on the same architectural layer
- **Fair API Testing**: Both APIs access identical data sources for objective comparison
- **Modern UI**: Material Design with dark mode support
- **MVVM Architecture**: Clean separation of concerns
- **Real-time Performance**: Kotlin Coroutines for async operations
- **Image Handling**: Consistent URL-based image loading across all entities

### **API Comparison Features**
- **Layer Consistency**: Both REST and GraphQL implemented at Repository layer
- **Identical Data Access**: Same PostgreSQL database for both APIs
- **Unified Error Handling**: Consistent error responses across APIs
- **Performance Metrics**: Built-in timing for API comparison

### **Enhanced Reseller Map Features**
- ✅ **Limited Reseller Loading**: Load up to 300 random resellers on map initialization
- ✅ **Search Capability**:
    - By **Reseller Name** (full database search)
    - By **City** (full database search)
    - By **Province** (with zoom functionality using Geocoder)
- ✅ **Search Filter Dropdown**: Select between **Reseller Name** and **City**
- ✅ **Search Results Dialog**: Shows up to 50 results with clickable navigation
- ✅ **Smart Zooming**: Automatically zooms to reseller or province on result tap

---

## 🛠 Tech Stack

### **Backend**
- **Node.js** with Express
- **PostgreSQL** database
- **Apollo Server** for GraphQL
- **RESTful API** endpoints

### **Frontend (Android)**
- **Kotlin** with Android Architecture Components
- **Retrofit** for REST API
- **Apollo Client** for GraphQL
- **Glide** for image loading
- **Material Design Components**

---

## 📁 Project Structure

Skincare-App/
├── app/
│ └── src/main/
│ ├── java/com/skincare/apitest/
│ │ ├── model/ # Data models
│ │ ├── network/ # Retrofit & Apollo clients
│ │ ├── repository/ # Repository layer (both APIs)
│ │ ├── ui/ # UI components
│ │ ├── viewmodel/ # ViewModels
│ │ └── MainActivity.kt
│ ├── graphql/ # GraphQL queries & schema
│ └── res/ # UI resources
├── backend/
│ └── server.js # Express server with REST & GraphQL
├── schema.graphqls # GraphQL schema
└── README.md

pgsql
Copy
Edit

---

## 🗄 Database Schema

### **PostgreSQL Tables**
```sql
-- Individual products
CREATE TABLE individual_products (
    id SERIAL PRIMARY KEY,
    product_name TEXT NOT NULL,
    description TEXT,
    price NUMERIC NOT NULL,
    image_data TEXT
);

-- Package products
CREATE TABLE package_products (
    id SERIAL PRIMARY KEY,
    package_name TEXT NOT NULL,
    items TEXT,
    price NUMERIC NOT NULL,
    image_data TEXT
);

-- Resellers
CREATE TABLE resellers (
    id SERIAL PRIMARY KEY,
    shop_name TEXT NOT NULL,
    profile_picture_url TEXT,
    reseller_name TEXT NOT NULL,
    whatsapp_number TEXT,
    facebook TEXT,
    instagram TEXT,
    city TEXT,
    latitude FLOAT NOT NULL,
    longitude FLOAT NOT NULL
);
🔌 API Endpoints
REST API
GET /api/products/individual

GET /api/products/package

GET /api/resellers/limited → (Random 300 resellers)

GET /api/resellers/search?name=...

GET /api/resellers/search?city=...

GraphQL
POST /graphql

graphql
Copy
Edit
query {
  limitedResellers {
    id
    shop_name
    ...
  }

  searchResellersByName(name: "Ayu") {
    ...
  }

  searchResellersByCity(city: "Malang") {
    ...
  }
}
🚀 Quick Setup
1. Backend Setup
bash
Copy
Edit
cd Skincare-App/backend
npm install

# Ensure PostgreSQL is running
# Create database: 'skincare_app'

node server.js
2. Android Setup
bash
Copy
Edit
# Open project in Android Studio
# Ensure base URLs are correct
# Build and run the app
3. Configuration
javascript
Copy
Edit
// backend/server.js
const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'skincare_app',
  password: 'password',
  port: 5432,
});
kotlin
Copy
Edit
// Android config
val REST_BASE_URL = "http://10.0.2.2:4000/api/"
val GRAPHQL_URL = "http://10.0.2.2:4000/graphql"
📊 API Comparison Architecture
Layer Design
kotlin
Copy
Edit
class ProductRepository {
    val retrofit = RetrofitClientProvider.getRetrofitClient()
    val apollo = ApolloClientProvider.getApolloClient()

    fun getProducts(api: ApiType): Flow<ApiResponse<List<Product>>> = flow {
        when (api) {
            ApiType.RETROFIT -> { /* REST */ }
            ApiType.GRAPHQL -> { /* GraphQL */ }
        }
    }
}
🎯 Usage Guide
1. Launch the App
Select API type (REST/GraphQL)

2. Test Features
Product & package listings

Reseller map view

3. Try Search & Comparison
Use province search bar

Use Reseller Name / City dropdown search

View map results and details dialog

🔍 Troubleshooting
Common Issues
400 Bad Request (GraphQL)
Check for schema/field mismatch

Database Not Connecting
Ensure PostgreSQL is active on port 5432

Android Emulator Network
Use 10.0.2.2 instead of localhost

📦 Contribution Guide
Fork this repo

Create a branch: feature/YourFeatureName

Commit your changes

Push and open a PR

📄 License
This project is licensed under the MIT License – see the LICENSE file.

🎓 Educational Purpose
This project is built for learning and exploration:

🔄 REST vs GraphQL side-by-side

🧱 MVVM + Repository pattern

📱 Modern Android with clean UI

🗃️ PostgreSQL database integration

✅ Update Summary:

Added endpoints for:

GET /api/resellers/limited

search by reseller name

search by city

Added frontend features for:

Province zoom

Dropdown search type

Search results dialog (up to 50)