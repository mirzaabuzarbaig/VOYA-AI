// scripts/test_locations_e2e.js
const http = require('http');

function post(url, body, headers = {}) {
  return new Promise((resolve, reject) => {
    const data = JSON.stringify(body);
    const parsed = new URL(url);
    const req = http.request({
      hostname: parsed.hostname,
      port: parsed.port,
      path: parsed.pathname + parsed.search,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(data),
        ...headers
      }
    }, res => {
      let respData = '';
      res.on('data', chunk => respData += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, body: JSON.parse(respData) });
        } catch (e) {
          resolve({ status: res.statusCode, body: respData });
        }
      });
    });
    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

function get(url, headers = {}) {
  return new Promise((resolve, reject) => {
    const parsed = new URL(url);
    const req = http.request({
      hostname: parsed.hostname,
      port: parsed.port,
      path: parsed.pathname + parsed.search,
      method: 'GET',
      headers
    }, res => {
      let respData = '';
      res.on('data', chunk => respData += chunk);
      res.on('end', () => {
        try {
          resolve({ status: res.statusCode, body: JSON.parse(respData) });
        } catch (e) {
          resolve({ status: res.statusCode, body: respData });
        }
      });
    });
    req.on('error', reject);
    req.end();
  });
}

async function run() {
  console.log('=== STARTING VOYA-AI INTEGRATION & VALIDATION TESTS ===\n');

  // 1. Auth (Register or Login)
  const testEmail = `e2e_tester_${Date.now()}@voyaai.com`;
  const regRes = await post('http://localhost:8080/api/auth/register', {
    name: 'E2E Tester',
    email: testEmail,
    password: 'password123'
  });
  let token;
  if (regRes.status === 201 || regRes.status === 200) {
    console.log(`[TEST 1: AUTH] Registered successfully: ${testEmail}`);
    const loginRes = await post('http://localhost:8080/api/auth/login', {
      email: testEmail,
      password: 'password123'
    });
    token = loginRes.body.token;
    console.log(`[TEST 1: AUTH] Login token obtained for: ${loginRes.body.user?.email}`);
  } else {
    console.log(`[TEST 1: AUTH] Register returned ${regRes.status}: ${JSON.stringify(regRes.body)}`);
  }

  // 2. Locations Autocomplete - Indian Destinations
  const ootyLocs = await get('http://localhost:8080/api/travel/locations?query=Ooty');
  console.log(`[TEST 2: INDIAN - Ooty] Status: ${ootyLocs.status}, Results: ${ootyLocs.body.length}`);
  if (ootyLocs.body.length > 0) {
    console.log(`  -> Selected: ${ootyLocs.body[0].displayName} (${ootyLocs.body[0].latitude}, ${ootyLocs.body[0].longitude})`);
  }

  const jaipurLocs = await get('http://localhost:8080/api/travel/locations?query=Jaipur');
  console.log(`[TEST 3: INDIAN - Jaipur] Status: ${jaipurLocs.status}, Results: ${jaipurLocs.body.length}`);
  if (jaipurLocs.body.length > 0) {
    console.log(`  -> Selected: ${jaipurLocs.body[0].displayName} (${jaipurLocs.body[0].latitude}, ${jaipurLocs.body[0].longitude})`);
  }

  const hydLocs = await get('http://localhost:8080/api/travel/locations?query=Hyderabad');
  console.log(`[TEST 4: INDIAN - Hyderabad] Status: ${hydLocs.status}, Results: ${hydLocs.body.length}`);
  if (hydLocs.body.length > 0) {
    console.log(`  -> Selected: ${hydLocs.body[0].displayName} (${hydLocs.body[0].latitude}, ${hydLocs.body[0].longitude})`);
  }

  // 3. Locations Autocomplete - International Destinations
  const parisLocs = await get('http://localhost:8080/api/travel/locations?query=Paris');
  console.log(`[TEST 5: INTERNATIONAL & AMBIGUOUS - Paris] Status: ${parisLocs.status}, Suggestions count: ${parisLocs.body.length}`);
  parisLocs.body.slice(0, 3).forEach(p => {
    console.log(`  -> Disambiguation: ${p.displayName} [${p.state || 'N/A'}, ${p.country}]`);
  });

  const tokyoLocs = await get('http://localhost:8080/api/travel/locations?query=Tokyo');
  console.log(`[TEST 6: INTERNATIONAL - Tokyo] Status: ${tokyoLocs.status}, Name: ${tokyoLocs.body[0]?.displayName}`);

  const dubaiLocs = await get('http://localhost:8080/api/travel/locations?query=Dubai');
  console.log(`[TEST 7: INTERNATIONAL - Dubai] Status: ${dubaiLocs.status}, Name: ${dubaiLocs.body[0]?.displayName}`);

  // 4. Short / Single Character Query
  const singleChar = await get('http://localhost:8080/api/travel/locations?query=a');
  console.log(`[TEST 8: MIN LENGTH GUARD] Query 'a' returned ${singleChar.body.length} items (expected 0)`);

  // 5. Places Endpoint Consistency (Verify NO Goa leakage for Ooty & Paris)
  const ootyPlacesRes = await get('http://localhost:8080/api/travel/places?destination=Ooty');
  const ootyPlaces = ootyPlacesRes.body.places || [];
  console.log(`[TEST 9: PLACES - Ooty] Status: ${ootyPlacesRes.status}, Places count: ${ootyPlaces.length}`);
  const hasGoaInOoty = ootyPlaces.some(p => (p.name + p.description).toLowerCase().includes('goa') || (p.name + p.description).toLowerCase().includes('calangute') || (p.name + p.description).toLowerCase().includes('baga'));
  console.log(`  -> Contains Goa places leaked? ${hasGoaInOoty ? 'FAIL (YES)' : 'PASS (NO)'}`);
  ootyPlaces.slice(0, 3).forEach(p => console.log(`     * ${p.name} - ${p.category}`));

  const parisPlacesRes = await get('http://localhost:8080/api/travel/places?destination=Paris');
  const parisPlaces = parisPlacesRes.body.places || [];
  console.log(`[TEST 10: PLACES - Paris] Status: ${parisPlacesRes.status}, Places count: ${parisPlaces.length}`);
  const hasGoaInParis = parisPlaces.some(p => (p.name + p.description).toLowerCase().includes('goa'));
  console.log(`  -> Contains Goa places leaked? ${hasGoaInParis ? 'FAIL (YES)' : 'PASS (NO)'}`);
  parisPlaces.slice(0, 3).forEach(p => console.log(`     * ${p.name} - ${p.category}`));

  // 6. Weather Synchronization
  const ootyWeather = await get('http://localhost:8080/api/travel/weather?destination=Ooty');
  console.log(`[TEST 11: WEATHER - Ooty] City: ${ootyWeather.body.city}, Temp: ${ootyWeather.body.temperature}C, Cond: ${ootyWeather.body.condition}`);

  const parisWeather = await get('http://localhost:8080/api/travel/weather?destination=Paris');
  console.log(`[TEST 12: WEATHER - Paris] City: ${parisWeather.body.city}, Temp: ${parisWeather.body.temperature}C, Cond: ${parisWeather.body.condition}`);

  // 7. Date Validation
  const now = new Date();
  const todayStr = now.toISOString().split('T')[0];
  const tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000).toISOString().split('T')[0];
  const fiveDaysOut = new Date(now.getTime() + 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  const rejectToday = await post('http://localhost:8080/api/trips', {
    destination: 'Ooty',
    source: 'Hyderabad',
    startDate: todayStr,
    endDate: fiveDaysOut,
    tripType: 'NATIONAL',
    budget: 35000,
    travelers: 2
  }, { Authorization: `Bearer ${token}` });
  console.log(`[TEST 13: DATE VALIDATION - Today Rejected] Status: ${rejectToday.status} (Expected 400), Message: ${JSON.stringify(rejectToday.body.message || rejectToday.body)}`);

  const acceptTomorrow = await post('http://localhost:8080/api/trips', {
    destination: 'Ooty',
    source: 'Hyderabad',
    startDate: tomorrow,
    endDate: fiveDaysOut,
    tripType: 'NATIONAL',
    budget: 35000,
    travelers: 2
  }, { Authorization: `Bearer ${token}` });
  console.log(`[TEST 14: DATE VALIDATION - Tomorrow Accepted] Status: ${acceptTomorrow.status} (Expected 200/201), Trip ID: ${acceptTomorrow.body.id}, Destination: ${acceptTomorrow.body.destination}, Source: ${acceptTomorrow.body.source}`);

  // 8. ML Cost Prediction for Ooty and Paris
  const mlOoty = await post('http://127.0.0.1:8000/predict', {
    durationDays: 5,
    travelers: 2,
    tripType: 'NATIONAL',
    destination: 'Ooty',
    month: 10,
    budget: 30000,
    currency: 'INR',
    accommodation: 'Hotel',
    transport: 'Flight'
  });
  console.log(`[TEST 15: ML PREDICTION - Ooty] Status: ${mlOoty.status}, Predicted Cost: ${mlOoty.body?.predictedCost} ${mlOoty.body?.currency} via ${mlOoty.body?.model}`);

  const mlParis = await post('http://127.0.0.1:8000/predict', {
    durationDays: 7,
    travelers: 2,
    tripType: 'INTERNATIONAL',
    destination: 'Paris',
    month: 10,
    budget: 180000,
    currency: 'INR',
    accommodation: 'Hotel',
    transport: 'Flight'
  });
  console.log(`[TEST 16: ML PREDICTION - Paris] Status: ${mlParis.status}, Predicted Cost: ${mlParis.body?.predictedCost} ${mlParis.body?.currency} via ${mlParis.body?.model}`);

  console.log('\n=== ALL INTEGRATION TESTS EXECUTED ===');
}

run().catch(console.error);
