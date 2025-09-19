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
    const limit = req.query.limit ? parseInt(req.query.limit) : null;
    let query = 'SELECT id, product_name, description, price, image_data FROM individual_products';
    let params = [];
    
    if (limit) {
      query += ' LIMIT $1';
      params.push(limit);
    }
    
    const result = await pool.query(query, params);
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

// REST API endpoint to get package products
app.get('/api/packages', async (req, res) => {
  try {
    const limit = req.query.limit ? parseInt(req.query.limit) : null;
    let query = 'SELECT id, package_name, items, price, image_data FROM package_products';
    let params = [];
    
    if (limit) {
      query += ' LIMIT $1';
      params.push(limit);
    }
    
    const result = await pool.query(query, params);
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
    res.json({ packages });
  } catch (error) {
    console.error('Error fetching packages:', error);
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

// REST API endpoint to get limited resellers for map (300 evenly distributed)
app.get('/api/resellers/limited', async (req, res) => {
  try {
    // Get all resellers first
    const result = await pool.query('SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers ORDER BY id');
    const allResellers = result.rows;

    // Distribute 300 markers evenly across Indonesia
    const limitedResellers = distributeResellersEvenly(allResellers, 300);

    const resellers = limitedResellers.map(row => ({
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
    console.error('Error fetching limited resellers:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// REST API endpoint to search resellers by name
app.get('/api/resellers/search/name/:query', async (req, res) => {
  try {
    const query = req.params.query;
    const result = await pool.query(
      'SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers WHERE LOWER(reseller_name) LIKE LOWER($1) LIMIT 50',
      [`%${query}%`]
    );
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
    console.error('Error searching resellers by name:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// REST API endpoint to search resellers by city
app.get('/api/resellers/search/city/:query', async (req, res) => {
  try {
    const query = req.params.query;
    const result = await pool.query(
      'SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers WHERE LOWER(city) LIKE LOWER($1) LIMIT 50',
      [`%${query}%`]
    );
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
    console.error('Error searching resellers by city:', error);
    res.status(500).json({ error: 'Internal server error' });
  }
});

// Helper function to distribute resellers evenly across Indonesia
function distributeResellersEvenly(resellers, limit) {
  if (resellers.length <= limit) {
    return resellers;
  }
// Create a grid system for Indonesia
  // Indonesia bounds: approximately lat: -11 to 6, lng: 95 to 141
  const latMin = -11, latMax = 6, lngMin = 95, lngMax = 141;
  const gridSize = Math.ceil(Math.sqrt(limit)); // Create a square grid
  const latStep = (latMax - latMin) / gridSize;
  const lngStep = (lngMax - lngMin) / gridSize;

  const grid = {};
  const selectedResellers = [];

  // Group resellers by grid cells
  resellers.forEach(reseller => {
    const latIndex = Math.floor((reseller.latitude - latMin) / latStep);
    const lngIndex = Math.floor((reseller.longitude - lngMin) / lngStep);
    const gridKey = `${latIndex}_${lngIndex}`;

    if (!grid[gridKey]) {
      grid[gridKey] = [];
    }
    grid[gridKey].push(reseller);
  });

  // Select one reseller from each grid cell (or more if we have space)
  const gridKeys = Object.keys(grid);
  const resellersPerCell = Math.max(1, Math.floor(limit / gridKeys.length));

  gridKeys.forEach(key => {
    const cellResellers = grid[key];
    const selectedCount = Math.min(resellersPerCell, cellResellers.length);

    // Randomly select resellers from this cell
    const shuffled = cellResellers.sort(() => 0.5 - Math.random());
    selectedResellers.push(...shuffled.slice(0, selectedCount));
  });

  // If we still need more resellers, add them randomly
  if (selectedResellers.length < limit) {
    const remaining = resellers.filter(r => !selectedResellers.includes(r));
    const shuffled = remaining.sort(() => 0.5 - Math.random());
    selectedResellers.push(...shuffled.slice(0, limit - selectedResellers.length));
  }

  return selectedResellers.slice(0, limit);
}

 // GraphQL resolvers
 const resolvers = {
   Query: {
     products: async (_, { limit }) => {
       try {
         let query = 'SELECT id, product_name, description, price, image_data FROM individual_products';
         let params = [];
         
         if (limit) {
           query += ' LIMIT $1';
           params.push(limit);
         }
         
         const result = await pool.query(query, params);
         return result.rows.map(row => ({
           id: row.id.toString(),
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
     packages: async (_, { limit }) => {
       try {
         let query = 'SELECT id, package_name, items, price, image_data FROM package_products';
         let params = [];
         
         if (limit) {
           query += ' LIMIT $1';
           params.push(limit);
         }
         
         const result = await pool.query(query, params);
         return result.rows.map(row => {
           let items = yaml.load(row.items);
           if (!Array.isArray(items)) {
             items = items ? [items] : [];
           }
           return {
             id: row.id.toString(),
             packageName: row.package_name,
             items: items,
             price: row.price,
             image_data: row.image_data || null,
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
           id: row.id.toString(),
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
    limitedResellers: async () => {
      try {
        const result = await pool.query('SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers ORDER BY id');
        const allResellers = result.rows;
        const limitedResellers = distributeResellersEvenly(allResellers, 300);

        return limitedResellers.map(row => ({
          id: row.id.toString(),
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
        console.error('Error fetching limited resellers:', error);
        throw new Error('Failed to fetch limited resellers');
      }
    },
    searchResellersByName: async (_, { query }) => {
      try {
        const result = await pool.query(
          'SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers WHERE LOWER(reseller_name) LIKE LOWER($1) LIMIT 50',
          [`%${query}%`]
        );
        return result.rows.map(row => ({
          id: row.id.toString(),
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
        console.error('Error searching resellers by name:', error);
        throw new Error('Failed to search resellers by name');
      }
    },
    searchResellersByCity: async (_, { query }) => {
      try {
        const result = await pool.query(
          'SELECT id, shop_name, profile_picture_url, reseller_name, whatsapp_number, facebook, instagram, city, latitude, longitude FROM resellers WHERE LOWER(city) LIKE LOWER($1) LIMIT 50',
          [`%${query}%`]
        );
        return result.rows.map(row => ({
          id: row.id.toString(),
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
        console.error('Error searching resellers by city:', error);
        throw new Error('Failed to search resellers by city');
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
