#!/usr/bin/env node
/**
 * Automate open-slide "Export as HTML" using Playwright.
 *
 * Expects the dev server to be running at http://localhost:5173/ and the
 * slide to be available at /s/ourbreak. Produces a standalone deployment
 * under deck/export/ (index.html + assets/).
 *
 * Local usage with ungoogled-chromium:
 *   CHROMIUM_EXECUTABLE=/Applications/Chromium.app/Contents/MacOS/Chromium node scripts/export-html.mjs
 */
import { chromium } from 'playwright';
import fs from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execSync } from 'node:child_process';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const OUT_DIR = path.join(ROOT, 'export');
const SLIDE_URL = process.env.SLIDE_URL || 'http://localhost:5173/s/ourbreak';
const CHROMIUM = process.env.CHROMIUM_EXECUTABLE || '';
const SLIDE_ID = 'ourbreak';

async function rmRf(dir) {
  try {
    await fs.rm(dir, { recursive: true, force: true });
  } catch {
    // ignore
  }
}

async function main() {
  await rmRf(OUT_DIR);
  await fs.mkdir(OUT_DIR, { recursive: true });

  const launchOptions = CHROMIUM ? { executablePath: CHROMIUM } : {};
  const browser = await chromium.launch(launchOptions);

  let downloadedPath = null;

  try {
    const context = await browser.newContext({ acceptDownloads: true });
    const page = await context.newPage();

    page.on('download', async (download) => {
      downloadedPath = path.join(OUT_DIR, `${SLIDE_ID}.zip`);
      await download.saveAs(downloadedPath);
    });

    console.log(`Opening ${SLIDE_URL} ...`);
    await page.goto(SLIDE_URL, { waitUntil: 'networkidle' });
    await page.waitForTimeout(1000);

    console.log('Clicking Download menu ...');
    await page.getByRole('button', { name: /download/i }).click();
    await page.waitForTimeout(300);

    console.log('Clicking Export as HTML ...');
    await page.getByRole('menuitem', { name: /export as html/i }).click();

    console.log('Waiting for download ...');
    let waited = 0;
    while (!downloadedPath && waited < 30000) {
      await page.waitForTimeout(500);
      waited += 500;
    }

    if (!downloadedPath) {
      throw new Error('Download did not start within 30 seconds');
    }
  } finally {
    await browser.close();
  }

  console.log(`Downloaded: ${downloadedPath}`);

  console.log('Extracting zip ...');
  execSync(`unzip -o "${downloadedPath}" -d "${OUT_DIR}"`, { stdio: 'inherit' });

  const htmlName = `${SLIDE_ID}.html`;
  const htmlPath = path.join(OUT_DIR, htmlName);
  const indexPath = path.join(OUT_DIR, 'index.html');

  await fs.rename(htmlPath, indexPath);
  await fs.rm(downloadedPath, { force: true });

  console.log(`Standalone presentation ready at: ${OUT_DIR}/`);
  console.log(`  - index.html`);
  const entries = await fs.readdir(OUT_DIR);
  for (const entry of entries.sort()) {
    if (entry === 'index.html') continue;
    console.log(`  - ${entry}`);
  }
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
