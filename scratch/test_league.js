const baseUrl = 'https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io';

async function run() {
    try {
        // 1. Register a test user
        const r1 = Math.random().toString(36).substring(7);
        const username = `testuser_${r1}`;
        console.log(`Registering ${username}...`);
        await fetch(`${baseUrl}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username,
                email: `${username}@test.com`,
                password: 'password123'
            })
        });

        // 2. Login
        console.log(`Logging in ${username}...`);
        const loginRes = await fetch(`${baseUrl}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username,
                password: 'password123'
            })
        });
        const loginData = await loginRes.json();
        const token = loginData.accessToken;
        console.log(`Got token: ${token.substring(0, 15)}...`);

        // 3. Create League with 2000000 RP
        console.log(`Creating league...`);
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const leagueRes = await fetch(`${baseUrl}/leagues`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                name: `League ${r1}`,
                season: 'S1',
                startDate: tomorrow.toISOString().split('T')[0],
                initialRp: 2000000,
                maxClubs: 10,
                transferRules: 'OPEN',
                matchWagerRp: 500,
                visibility: 'PUBLIC'
            })
        });
        const league = await leagueRes.json();
        console.log(`Created League:`, league);

        // 4. Create Club
        console.log(`Creating club...`);
        const clubRes = await fetch(`${baseUrl}/clubs`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                name: `Club ${r1}`,
                acronym: r1.substring(0, 4).toUpperCase(),
                division: 'BRONZE',
                initialRp: league.initialRp
            })
        });
        const club = await clubRes.json();
        console.log(`Created Club:`, club);

    } catch (e) {
        console.error("Error:", e);
    }
}

run();
