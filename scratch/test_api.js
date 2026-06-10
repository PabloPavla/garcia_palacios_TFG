const axios = require('axios');

async function main() {
    try {
        // 1. Login to get token
        const loginRes = await axios.post('https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io/auth/login', {
            username: 'pablo',
            password: 'password'
        });
        const token = loginRes.data.accessToken;

        // 2. Fetch leagues
        const leaguesRes = await axios.get('https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io/leagues', {
            headers: { Authorization: `Bearer ${token}` }
        });

        console.log("Leagues:", JSON.stringify(leaguesRes.data.slice(0, 2), null, 2));

    } catch (e) {
        console.error(e.response ? e.response.data : e.message);
    }
}
main();
