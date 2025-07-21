# Skincare App - API Comparison Project

An Android application that demonstrates **dual API implementation** (REST and GraphQL) for fetching skincare products from a PostgreSQL database, designed for **fair performance comparison**.

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

## 📁 Project Structure

```
Skincare-App/
├── app/
│   └── src/main/
│       ├── java/com/skincare/apitest/
│       │   ├── model/           # Data models
│       │   ├── network/         # Retrofit & Apollo clients
│       │   ├── repository/      # Repository layer (both APIs)
│       │   ├── ui/             # UI components
│       │   ├── viewmodel/      # ViewModels
│       │   └── MainActivity.kt
│       ├── graphql/            # GraphQL queries & schema
│       └── res/               # UI resources
├── backend/
│   └── server.js             # Express server with REST & GraphQL
├── schema.graphqls           # GraphQL schema
└── README.md
```

## 🗄 Database Schema

### **PostgreSQL Tables**
```sql
-- Individual products
CREATE TABLE individual_products (
    id SERIAL PRIMARY KEY,
    product_name TEXT NOT NULL,
    description TEXT,
    price NUMERIC NOT NULL,
    image_data TEXT  -- URL string, not base64
);

-- Package products
CREATE TABLE package_products (
    id SERIAL PRIMARY KEY,
    package_name TEXT NOT NULL,
    items TEXT,  -- YAML format
    price NUMERIC NOT NULL,
    image_data TEXT  -- URL string, not base64
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
```

## 🚀 Quick Setup

### **1. Backend Setup**
```bash
# Navigate to backend directory
cd Skincare-App/backend

# Install dependencies
npm install

# Start PostgreSQL server
# Create database 'skincare_app' with user 'postgres' and password 'password'

# Start backend server
node server.js
```

### **2. Android Setup**
```bash
# Open in Android Studio
# Update database connection in backend/server.js if needed
# Build and run the app
```

### **3. API Endpoints**
- **REST API**: `http://localhost:4000/api/`
- **GraphQL**: `http://localhost:4000/graphql`

## 🔧 API Configuration

### **Database Connection**
Update connection details in `backend/server.js`:
```javascript
const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'skincare_app',
  password: 'password',
  port: 5432,
});
```

### **Android Configuration**
- **REST Base URL**: `http://10.0.2.2:4000/api/`
- **GraphQL URL**: `http://10.0.2.2:4000/graphql`

## 📊 API Comparison Architecture

### **Layer Consistency**
Both REST and GraphQL are implemented at the **Repository Layer**:

```kotlin
class ProductRepository {
    private val retrofitService = RetrofitClientProvider.getRetrofitClient()
    private val apolloClient = ApolloClientProvider.getApolloClient()
    
    fun getProducts(apiType: ApiType): Flow<ApiResponse<List<Product>>> = flow {
        when (apiType) {
            ApiType.RETROFIT -> { /* REST implementation */ }
            ApiType.GRAPHQL -> { /* GraphQL implementation */ }
        }
    }
}
```

### **Data Consistency**
- ✅ **Same database**: PostgreSQL for both APIs
- ✅ **Same data**: Identical product/package/reseller data
- ✅ **Same layer**: Repository layer implementation
- ✅ **Same error handling**: Unified ApiResponse sealed class

## 🎯 Usage Guide

### **1. Launch App**
- Open the app on Android device/emulator
- Select API type from dropdown (REST/GraphQL)

### **2. Test APIs**
- **Individual Products**: View single products
- **Package Products**: View product packages
- **Resellers**: View reseller locations

### **3. Performance Comparison**
- Both APIs fetch identical data
- Built-in timing for performance metrics
- Same error handling for fair comparison

## 🔍 Troubleshooting

### **Common Issues**

#### **Error 400 - Bad Request**
- **Cause**: Field name mismatch between schema and query
- **Solution**: Ensure field names match exactly:
    - Schema: `image_data`
    - Query: `image_data`
    - Resolver: `image_data`

#### **Database Connection**
- **Check**: PostgreSQL running on port 5432
- **Verify**: Database 'skincare_app' exists
- **Test**: Connection with provided credentials

#### **Android Emulator**
- **Use**: `10.0.2.2` instead of `localhost`
- **Check**: Network permissions in AndroidManifest.xml

### **Debug Steps**
1. **Test GraphQL directly**:
   ```bash
   curl -X POST http://localhost:4000/graphql \
     -H "Content-Type: application/json" \
     -d '{"query":"{ packages { id packageName items price image_data } }"}'
   ```

2. **Check server logs**:
   ```bash
   node server.js
   ```

3. **Verify database**:
   ```sql
   SELECT * FROM package_products LIMIT 1;
   ```

## 📝 Contributing

1. **Fork** the repository
2. **Create** feature branch: `git checkout -b feature/AmazingFeature`
3. **Commit** changes: `git commit -m 'Add AmazingFeature'`
4. **Push** to branch: `git push origin feature/AmazingFeature`
5. **Open** Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🎓 Educational Purpose

This project is designed for **educational purposes** to demonstrate:
- **Fair API comparison** between REST and GraphQL
- **Clean architecture** implementation
- **Modern Android development** practices
- **Database integration** with PostgreSQL

### **Key Learning Points**
- ✅ REST vs GraphQL performance comparison
- ✅ Repository pattern implementation
- ✅ MVVM architecture
- ✅ Database design and integration
- ✅ Modern Android development stack
