const axios = require('axios');

const BASE = 'https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io';

async function main() {
    try {
        // Register
        const ts = Date.now();
        const regResp = await axios.post(BASE + '/auth/register', {
            username: 'e2etest_' + ts,
            email: 'e2etest_' + ts + '@test.com',
            password: 'TestPass123!'
        });
        const token = regResp.data.accessToken;
        const headers = { Authorization: 'Bearer ' + token };
        
        console.log('User registered, userId:', regResp.data.userId);
        
        // Create league with specific initialRp
        const targetRp = 500000;
        const leagueResp = await axios.post(BASE + '/leagues', {
            name: 'E2ETestLeague_' + Date.now(),
            season: '2024',
            initialRp: targetRp,
            maxClubs: 4,
            matchWagerRp: 500,
            startDate: '2026-06-11',
            visibility: 'PUBLIC'
        }, { headers });
        
        const league = leagueResp.data;
        console.log('League created:', { id: league.id, initialRp: league.initialRp });
        
        // Join league
        await axios.post(BASE + '/leagues/' + league.id + '/join', {}, { headers });
        console.log('Joined league');
        
        // Create club with initialRp from league
        const clubResp = await axios.post(BASE + '/clubs', {
            name: 'E2EClub_' + Date.now(),
            acronym: 'E2EC',
            division: 'BRONZE',
            initialRp: league.initialRp
        }, { headers });
        
        const club = clubResp.data;
        console.log('Club created:', { id: club.id, riotPoints: club.riotPoints });
        console.log('EXPECTED:', targetRp, '| ACTUAL:', club.riotPoints, '| MATCH:', club.riotPoints === targetRp ? 'YES ✅' : 'NO ❌');
        
    } catch (e) {
        console.error('Error:', e.response?.data || e.message);
    }
}

main();
