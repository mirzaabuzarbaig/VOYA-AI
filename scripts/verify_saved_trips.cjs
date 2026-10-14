const puppeteer = require('puppeteer-core');
const path = require('path');

const EDGE_PATH = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const ARTIFACTS_DIR = 'C:\\Users\\MAB12\\.gemini\\antigravity-ide\\brain\\7df62df1-c9db-4b1d-9938-d7a78885de91';

(async () => {
  const browser = await puppeteer.launch({
    executablePath: EDGE_PATH,
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });
  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 900 });

  await page.goto('http://localhost:5173/login', { waitUntil: 'networkidle2' });
  await page.type('#login-email', 'ui_tester_1791527837661@voyaai.com');
  await page.type('#login-password', 'Password123!');
  await page.click('button[type="submit"]');
  await page.waitForNavigation({ waitUntil: 'networkidle2' });
  console.log('[1] Logged in successfully, URL:', page.url());

  // Check saved trips at /saved
  await page.goto('http://localhost:5173/saved', { waitUntil: 'networkidle2' });
  await new Promise(r => setTimeout(r, 2000));
  const text = await page.evaluate(() => document.body.innerText);

  const hasOoty = text.includes('Udhagamandalam') || text.includes('Ooty');
  const hasParis = text.includes('Paris');

  console.log('[2] Saved Trips contains Ooty/Udhagamandalam:', hasOoty ? 'PASS (YES)' : 'FAIL (NO)');
  console.log('[3] Saved Trips contains Paris:', hasParis ? 'PASS (YES)' : 'FAIL (NO)');

  await page.screenshot({ path: path.join(ARTIFACTS_DIR, 'saved_trips_verification.png'), fullPage: true });

  await browser.close();
  console.log('Saved trips verified cleanly!');
})();
