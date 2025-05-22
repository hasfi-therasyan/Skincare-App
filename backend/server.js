const express = require('express');
const { ApolloServer, gql } = require('apollo-server-express');
const cors = require('cors');
const { Pool } = require('pg');

// PostgreSQL connection pool
const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'skincare_app',
  password: 'password',
  port: 5432,
});

// Express app setup
const app = express();
app.use(cors());
app.use(express.json());

// REST API endpoint to get products
app.get('/api/products', async (req, res) => {
  try {
    const result = await pool.query('SELECT id, product_name, description, price, image_data FROM individual_products');
    const products = result.rows.map(row => ({
      id: row.id,
      product_name: row.product_name,
      description: row.description,
      price: row.price,
      image_data: row.image_data ? row.image_data.toString('base64') : null,
    }));
    res.json({ products });
  } catch (error) {
    console.error('Error fetching products:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// GraphQL type definitions
const typeDefs = gql`
  type Product {
    id: ID!
    product_name: String
    description: String
    price: Float
    image_data: String
  }

  type Query {
    products: [Product]
  }
`;

// GraphQL resolvers
const resolvers = {
  Query: {
    products: async () => {
      try {
        const result = await pool.query('SELECT id, product_name, description, price, image_data FROM individual_products');
        return result.rows.map(row => ({
          id: row.id,
          product_name: row.product_name,
          description: row.description,
          price: row.price,
          image_data: row.image_data ? row.image_data.toString('base64') : null,
        }));
      } catch (error) {
        console.error('Error fetching products:', error);
        throw new Error('Failed to fetch products');
      }
    },
  },
};

// Apollo Server setup
async function startApolloServer() {
  const server = new ApolloServer({ typeDefs, resolvers });
  await server.start();
  server.applyMiddleware({ app, path: '/graphql' });

  const PORT = 4000;
  app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
    console.log(`GraphQL endpoint at http://localhost:${PORT}/graphql`);
  });
}

startApolloServer();
