<<<<<<< HEAD
# Skincare App

An Android application that demonstrates dual API implementation (REST and GraphQL) for fetching skincare products from a PostgreSQL database.

## Features

- Dual API support:
  - REST API using Retrofit
  - GraphQL using Apollo Client
- Modern UI with Material Design
- MVVM Architecture
- Kotlin Coroutines for async operations
- Image loading with Glide
- Dark mode support

## Tech Stack

- Kotlin
- Android Architecture Components
  - ViewModel
  - LiveData
  - Coroutines
- Retrofit for REST API
- Apollo for GraphQL
- Glide for image loading
- Material Design Components

## Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/skincare/apitest/
│       │   ├── model/
│       │   │   └── Product.kt
│       │   ├── network/
│       │   │   └── ProductService.kt
│       │   ├── repository/
│       │   │   └── ProductRepository.kt
│       │   ├── ui/
│       │   │   └── ProductAdapter.kt
│       │   ├── viewmodel/
│       │   │   └── ProductViewModel.kt
│       │   └── MainActivity.kt
│       └── graphql/
│           └── com/skincare/apitest/
│               └── GetProducts.graphql
```

## Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/skincare-app.git
```

2. Open the project in Android Studio

3. Update the API endpoints:
   - REST API: Update `BASE_URL` in `ProductService.kt`
   - GraphQL: Update `SERVER_URL` in `ProductService.kt`

4. Build and run the app

## Database Schema

The app expects a PostgreSQL database with the following table structure:

```sql
CREATE TABLE individual_products (
    id SERIAL PRIMARY KEY,
    product_name TEXT,
    description TEXT,
    price NUMERIC,
    image_data BYTEA
);
```

## Usage

1. Launch the app
2. Select the API type (REST or GraphQL) from the dropdown
3. Click "Fetch Products" to load the product list
4. View product details including images, descriptions, and prices

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
=======
This is a [Next.js](https://nextjs.org) project bootstrapped with [`create-next-app`](https://nextjs.org/docs/app/api-reference/cli/create-next-app).

## Getting Started

First, run the development server:

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
# or
bun dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.

You can start editing the page by modifying `app/page.tsx`. The page auto-updates as you edit the file.

This project uses [`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts) to automatically optimize and load [Geist](https://vercel.com/font), a new font family for Vercel.

## Learn More

To learn more about Next.js, take a look at the following resources:

- [Next.js Documentation](https://nextjs.org/docs) - learn about Next.js features and API.
- [Learn Next.js](https://nextjs.org/learn) - an interactive Next.js tutorial.

You can check out [the Next.js GitHub repository](https://github.com/vercel/next.js) - your feedback and contributions are welcome!

## Deploy on Vercel

The easiest way to deploy your Next.js app is to use the [Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme) from the creators of Next.js.

Check out our [Next.js deployment documentation](https://nextjs.org/docs/app/building-your-application/deploying) for more details.
>>>>>>> 28806c9 (Add product detail pop-out feature on product item click with cart button)
