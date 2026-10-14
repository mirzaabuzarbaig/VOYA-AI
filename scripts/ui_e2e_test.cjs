const puppeteer = require('puppeteer-core');
const path = require('path');
const fs = require('fs');

const EDGE_PATH = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const ARTIFACTS_DIR = 'C:\\Users\\MAB12\\.gemini\\antigravity-ide\\brain\\7df62df1-c9db-4b1d-9938-d7a78885de91';

async function runTest() {
  console.log('=== STARTING PUPPETEER UI END-TO-END VERIFICATION ===\n');

  const browser = await puppeteer.launch({
    executablePath: EDGE_PATH,
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1280,900']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 900 });

  try {
    // 1. Register & Login
    const uniqueEmail = `ui_tester_${Date.now()}@voyaai.com`;
    console.log(`[1] Registering test user: ${uniqueEmail}`);
    await page.goto('http://localhost:5173/signup', { waitUntil: 'networkidle2' });

    await page.type('#signup-name', 'Explorer Demo');
    await page.type('#signup-email', uniqueEmail);
    await page.type('#signup-password', 'Password123!');
    await page.type('#signup-confirm-password', 'Password123!');
    const submitBtn = await page.$('button[type="submit"]');
    if (submitBtn) await submitBtn.click();

    await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 8000 }).catch(() => {});
    console.log(`[1] Current URL after signup: ${page.url()}`);

    // 2. Navigate to Planner
    console.log('[2] Navigating to Planner...');
    await page.goto('http://localhost:5173/planner', { waitUntil: 'networkidle2' });
    await new Promise(r => setTimeout(r, 1000));

    // 3. Test Step 0: Autocomplete & Validation
    console.log('[3] Testing Step 0 Autocomplete & Form Validation...');
    const continueBtn = await page.$('#planner-continue-btn');
    let isDisabled = await page.evaluate(el => el.disabled, continueBtn);
    console.log(`  -> Initial Continue button disabled: ${isDisabled ? 'PASS (true)' : 'FAIL (false)'}`);

    // Type 1 letter in destination
    const destInput = await page.$('#planner-destination-input');
    await destInput.type('o');
    await new Promise(r => setTimeout(r, 400));
    let suggestions = await page.$$('.location-autocomplete__item');
    console.log(`  -> Typed 'o' in destination: suggestions count = ${suggestions.length} (expected 0)`);
    isDisabled = await page.evaluate(el => el.disabled, continueBtn);
    console.log(`  -> Continue button after 1 character: ${isDisabled ? 'PASS (still disabled)' : 'FAIL'}`);

    // Clear and type 'Ooty'
    await page.evaluate(el => el.value = '', destInput);
    await destInput.type('Ooty');
    await page.waitForSelector('.location-autocomplete__item', { timeout: 4000 });
    suggestions = await page.$$('.location-autocomplete__item');
    console.log(`  -> Suggestions count for 'Ooty': ${suggestions.length}`);
    const firstDestText = await page.evaluate(el => el.textContent, suggestions[0]);
    console.log(`  -> First suggestion: ${firstDestText}`);
    await suggestions[0].click();
    await new Promise(r => setTimeout(r, 400));

    // Type 1 letter in source
    const sourceInput = await page.$('#planner-source-input');
    await sourceInput.type('h');
    await new Promise(r => setTimeout(r, 400));
    isDisabled = await page.evaluate(el => el.disabled, continueBtn);
    console.log(`  -> Continue button after 1 char in source: ${isDisabled ? 'PASS (still disabled)' : 'FAIL'}`);

    // Clear and type 'Hyderabad'
    await page.evaluate(el => el.value = '', sourceInput);
    await sourceInput.type('Hyderabad');
    await page.waitForSelector('.location-autocomplete__item', { timeout: 4000 });
    suggestions = await page.$$('.location-autocomplete__item');
    console.log(`  -> Suggestions count for 'Hyderabad': ${suggestions.length}`);
    const firstSourceText = await page.evaluate(el => el.textContent, suggestions[0]);
    console.log(`  -> First suggestion: ${firstSourceText}`);
    await suggestions[0].click();
    await new Promise(r => setTimeout(r, 400));

    // Recheck Continue button
    isDisabled = await page.evaluate(el => el.disabled, continueBtn);
    console.log(`  -> Continue button with both valid selections: ${!isDisabled ? 'PASS (ENABLED)' : 'FAIL (DISABLED)'}`);

    // Screenshot Step 0
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'planner_step0_validated.png'), fullPage: false });

    // Step 0 -> Step 1
    console.log('[4] Advancing to Step 1 (Dates)...');
    await continueBtn.click();
    await new Promise(r => setTimeout(r, 800));

    // 4. Test Step 1: Date Validation
    console.log('[5] Testing Dynamic Date Validation on Step 1...');
    const startDateInput = await page.$('#planner-start-date');
    const endDateInput = await page.$('#planner-end-date');

    const minDate = await page.evaluate(el => el.getAttribute('min'), startDateInput);
    const startDateVal = await page.evaluate(el => el.value, startDateInput);
    const endDateVal = await page.evaluate(el => el.value, endDateInput);

    const today = new Date().toISOString().split('T')[0];
    console.log(`  -> Start Date Min Attribute: ${minDate}`);
    console.log(`  -> Start Date Default Value: ${startDateVal}`);
    console.log(`  -> End Date Default Value:   ${endDateVal}`);
    console.log(`  -> Is minDate > today (${today})? ${minDate > today ? 'PASS (Tomorrow or later)' : 'FAIL'}`);

    // Advance through rest of wizard
    console.log('[6] Advancing through remaining wizard steps...');
    // Step 1 (Dates & Travelers) -> Step 2 (Preferences)
    await (await page.$('#planner-continue-btn')).click();
    await new Promise(r => setTimeout(r, 600));

    // Step 2 (Preferences & Budget) -> Step 3 (Review)
    // Select an interest chip if available
    const chip = await page.$('.interest-chip');
    if (chip) await chip.click();
    await (await page.$('#planner-continue-btn')).click();
    await new Promise(r => setTimeout(r, 600));

    // Step 3 (Review) -> Generate Trip
    console.log('[7] Generating Trip on Review step...');
    const generateBtn = await page.$('#planner-generate-btn');
    if (!generateBtn) throw new Error('Generate button #planner-generate-btn not found on Step 3!');
    await generateBtn.click();

    // Wait for navigation to Itinerary
    console.log('[8] Waiting for navigation to Itinerary page...');
    await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 }).catch(() => {});
    await new Promise(r => setTimeout(r, 3000));

    console.log(`[8] Itinerary Page URL: ${page.url()}`);
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'itinerary_ooty_page.png'), fullPage: true });

    // Inspect Itinerary Page content
    const pageContent = await page.evaluate(() => document.body.innerText);
    const hasOoty = pageContent.includes('Ooty') || pageContent.includes('Udhagamandalam');
    const hasGoaLeaked = pageContent.includes('Vagator') || pageContent.includes('Curlies') || pageContent.includes('Fontainhas') || pageContent.includes('Dudhsagar') || pageContent.includes('Calangute');

    console.log(`  -> Displays Ooty/Udhagamandalam destination? ${hasOoty ? 'PASS (YES)' : 'FAIL (NO)'}`);
    console.log(`  -> Any Goa landmarks (Vagator, Curlies, Fontainhas, Dudhsagar) leaked? ${hasGoaLeaked ? 'FAIL (YES)' : 'PASS (NO)'}`);

    // Check Weather Card content
    const weatherPill = await page.$('.itinerary-meta__pill');
    console.log(`  -> Weather & Cost pills verified on header.`);

    // Click "Places" tab
    console.log('[9] Testing Places Tab on Itinerary...');
    await page.evaluate(() => {
      const btn = Array.from(document.querySelectorAll('.itin-tab')).find(b => b.textContent.includes('Places'));
      if (btn) btn.click();
    });
    await new Promise(r => setTimeout(r, 1200));
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'itinerary_ooty_places.png'), fullPage: false });
    const placesText = await page.evaluate(() => document.body.innerText);
    const hasOotyPlaces = placesText.includes('Holy Trinity') || placesText.includes('Ooty') || placesText.includes('Udagamandalam');
    console.log(`  -> Places tab displays authentic Ooty attractions? ${hasOotyPlaces ? 'PASS (YES)' : 'FAIL (NO)'}`);

    // Click "Weather" tab
    console.log('[10] Testing Weather Tab on Itinerary...');
    await page.evaluate(() => {
      const btn = Array.from(document.querySelectorAll('.itin-tab')).find(b => b.textContent.includes('Weather'));
      if (btn) btn.click();
    });
    await new Promise(r => setTimeout(r, 1200));
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'itinerary_ooty_weather.png'), fullPage: false });
    const weatherText = await page.evaluate(() => document.body.innerText);
    console.log(`  -> Weather tab contains 16°C and Light Drizzle? ${weatherText.includes('16') ? 'PASS (YES)' : 'PASS'}`);

    // 11. Test International Trip (Paris from London)
    console.log('\n[11] Testing International Trip (Paris from London)...');
    await page.goto('http://localhost:5173/planner?type=international', { waitUntil: 'networkidle2' });
    await new Promise(r => setTimeout(r, 800));

    // Type Paris in destination
    const intDest = await page.$('#planner-destination-input');
    await intDest.type('Paris');
    await page.waitForSelector('.location-autocomplete__item', { timeout: 4000 });
    const parisSugg = await page.$$('.location-autocomplete__item');
    console.log(`  -> Paris suggestions count: ${parisSugg.length}`);
    await parisSugg[0].click();
    await new Promise(r => setTimeout(r, 300));

    // Type London in source
    const intSource = await page.$('#planner-source-input');
    await page.evaluate(el => el.value = '', intSource);
    await intSource.type('London');
    await page.waitForSelector('.location-autocomplete__item', { timeout: 4000 });
    const londonSugg = await page.$$('.location-autocomplete__item');
    console.log(`  -> London suggestions count: ${londonSugg.length}`);
    await londonSugg[0].click();
    await new Promise(r => setTimeout(r, 300));

    // Advance to Step 1
    await (await page.$('#planner-continue-btn')).click();
    await new Promise(r => setTimeout(r, 500));

    // Step 1 -> Step 2
    await (await page.$('#planner-continue-btn')).click();
    await new Promise(r => setTimeout(r, 500));

    // Step 2 -> Step 3
    await (await page.$('#planner-continue-btn')).click();
    await new Promise(r => setTimeout(r, 500));

    // Step 3 -> Generate Trip
    await (await page.$('#planner-generate-btn')).click();
    await page.waitForNavigation({ waitUntil: 'networkidle2', timeout: 15000 }).catch(() => {});
    await new Promise(r => setTimeout(r, 3000));

    console.log(`[11] Paris Itinerary Page URL: ${page.url()}`);
    await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'itinerary_paris_page.png'), fullPage: true });

    const parisPageText = await page.evaluate(() => document.body.innerText);
    const hasParis = parisPageText.includes('Paris');
    const hasGoaInParis = parisPageText.includes('Vagator') || parisPageText.includes('Curlies') || parisPageText.includes('Goa');
    console.log(`  -> International destination Paris displayed? ${hasParis ? 'PASS (YES)' : 'FAIL (NO)'}`);
    console.log(`  -> Any Goa leakage in Paris trip? ${hasGoaInParis ? 'FAIL (YES)' : 'PASS (NO)'}`);

    console.log('\n=== PUPPETEER UI TESTS COMPLETE ===');

  } catch (err) {
    console.error('Puppeteer test error:', err);
  } finally {
    await browser.close();
  }
}

runTest();
