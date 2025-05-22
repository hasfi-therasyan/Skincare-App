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

// REST API endpoint to get individual products
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

// REST API endpoint to get package products
app.get('/api/packages', async (req, res) => {
  try {
    const result = await pool.query('SELECT id, package_name, items, price, image_data FROM package_products');
    const packages = result.rows.map(row => ({
      id: row.id,
      package_name: row.package_name,
      items: row.items,
      price: row.price,
      image_data: row.image_data ? row.image_data.toString('base64') : null,
    }));
    res.json({ packages });
  } catch (error) {
    console.error('Error fetching packages:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// REST API endpoint to get individual product image by id
app.get('/api/product/image/:id', async (req, res) => {
  const id = req.params.id;
  try {
    const result = await pool.query('SELECT image_data FROM individual_products WHERE id = $1', [id]);
    if (result.rows.length > 0) {
      const imageData = result.rows[0].image_data;
      if (imageData) {
        res.send(imageData);
      } else {
        res.status(404).send('Image not found');
      }
    } else {
      res.status(404).send('Product not found');
    }
  } catch (error) {
    console.error('Error fetching product image:', error);
    res.status(500).send('Internal server error');
  }
});

// REST API endpoint to get package product image by id
app.get('/api/package/image/:id', async (req, res) => {
  const id = req.params.id;
  try {
    const result = await pool.query('SELECT image_data FROM package_products WHERE id = $1', [id]);
    if (result.rows.length > 0) {
      const imageData = result.rows[0].image_data;
      if (imageData) {
        res.send(imageData);
      } else {
        res.status(404).send('Image not found');
      }
    } else {
      res.status(404).send('Package not found');
    }
  } catch (error) {
    console.error('Error fetching package image:', error);
    res.status(500).send('Internal server error');
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

  type Package {
    id: ID!
    packageName: String
    items: [String]
    price: Float
    image: String
  }

  type Query {
    products: [Product]
    packages: [Package]
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
    packages: async () => {
      try {
        const result = await pool.query('SELECT id, package_name, items, price, image_data FROM package_products');
        return result.rows.map(row => ({
          id: row.id,
          packageName: row.package_name,
          items: row.items,
          price: row.price,
          image: row.image_data ? row.image_data.toString('base64') : null,
        }));
      } catch (error) {
        console.error('Error fetching packages:', error);
        throw new Error('Failed to fetch packages');
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
