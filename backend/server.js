const express = require('express');
const { ApolloServer, gql } = require('apollo-server-express');
const cors = require('cors');
const { Pool } = require('pg');
const fs = require('fs');
const path = require('path');
const yaml = require('js-yaml');

// PostgreSQL connection pool
const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'skincare_app',
  password: 'password',
  port: 5432,
});

// Load GraphQL schema from schema.graphqls file
const schemaPath = path.join(__dirname, '../app/src/main/graphql/schema.graphqls');
const typeDefs = gql(fs.readFileSync(schemaPath, { encoding: 'utf-8' }));

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
      image_data: row.image_data || null,
    }));
    res.json({ products });
  } catch (error) {
    console.error('Error fetching products:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// REST API endpoint to get resellers
app.get('/api/resellers', async (req, res) => {
  try {
    const result = await pool.query('SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers');
    const resellers = result.rows.map(row => ({
      id: row.id,
      shop_name: row.shop_name,
      profile_picture_url: row.profile_picture_url,
      reseller_name: row.reseller_name,
      whatsapp_number: row.whatsapp_number,
      facebook: row.facebook,
      instagram: row.instagram,
      city: row.city,
      latitude: row.latitude,
      longitude: row.longitude,
    }));
    res.json({ resellers });
  } catch (error) {
    console.error('Error fetching resellers:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// REST API endpoint to get package products
app.get('/api/packages', async (req, res) => {
  try {
    const result = await pool.query('SELECT id, package_name, items, price, image_data FROM package_products');
    const packages = result.rows.map(row => {
      let items = yaml.load(row.items);
      if (!Array.isArray(items)) {
        items = items ? [items] : [];
      }
      return {
        id: row.id,
        package_name: row.package_name,
        items: items,
        price: row.price,
        image_data: row.image_data || null,
      };
    });
    console.log('Packages response:', JSON.stringify({ packages }));
    res.json({ packages });
  } catch (error) {
    console.error('Error fetching packages:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// REST API endpoint to get individual products
app.get('/api/products', async (req, res) => {
  try {
    const result = await pool.query('SELECT id, product_name, description, price, image_data FROM individual_products');
    const products = result.rows.map(row => ({
      id: row.id,
      product_name: row.product_name,
      description: row.description,
      price: row.price,
      image_url: row.image_data || null,
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
    const packages = result.rows.map(row => {
      let items = yaml.load(row.items);
      if (!Array.isArray(items)) {
        items = items ? [items] : [];
      }
      return {
        id: row.id,
        package_name: row.package_name,
        items: items,
        price: row.price,
        image_url: row.image_data || null,
      };
    });
    res.json({ packages });
  } catch (error) {
    console.error('Error fetching packages:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

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
          image_data: row.image_data || null,
        }));
      } catch (error) {
        console.error('Error fetching products:', error);
        throw new Error('Failed to fetch products');
      }
    },
    packages: async () => {
      try {
        const result = await pool.query('SELECT id, package_name, items, price, image_data FROM package_products');
        return result.rows.map(row => {
          let items = yaml.load(row.items);
          if (!Array.isArray(items)) {
            items = items ? [items] : [];
          }
          return {
            id: row.id,
            packageName: row.package_name,
            items: items,
            price: row.price,
            image: row.image_data || null,
          };
        });
      } catch (error) {
        console.error('Error fetching packages:', error);
        throw new Error('Failed to fetch packages');
      }
    },
    resellers: async () => {
      try {
        const result = await pool.query('SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers');
        return result.rows.map(row => ({
          id: row.id,
          shop_name: row.shop_name,
          profile_picture_url: row.profile_picture_url,
          reseller_name: row.reseller_name,
          whatsapp_number: row.whatsapp_number,
          facebook: row.facebook,
          instagram: row.instagram,
          city: row.city,
          latitude: row.latitude,
          longitude: row.longitude,
        }));
      } catch (error) {
        console.error('Error fetching resellers:', error);
        throw new Error('Failed to fetch resellers');
      }
    },
    productImage: async (_, { id }) => {
      try {
        const result = await pool.query('SELECT image_data FROM individual_products WHERE id = $1', [id]);
        if (result.rows.length > 0) {
          return { image_data: result.rows[0].image_data || null };
        }
        return null;
      } catch (error) {
        console.error('Error fetching product image:', error);
        throw new Error('Failed to fetch product image');
      }
    },
    packageImage: async (_, { id }) => {
      try {
        const result = await pool.query('SELECT image_data FROM package_products WHERE id = $1', [id]);
        if (result.rows.length > 0) {
          return { image_data: result.rows[0].image_data || null };
        }
        return null;
      } catch (error) {
        console.error('Error fetching package image:', error);
        throw new Error('Failed to fetch package image');
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