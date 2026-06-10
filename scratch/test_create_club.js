const axios = require('axios');

async function main() {
    try {
        const username = "testuser" + Math.floor(Math.random() * 10000);
        console.log("Registering:", username);
        await axios.post('https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io/auth/register', {
            username: username,
            email: username + "@test.com",
            password: "password123"
        });

        const loginRes = await axios.post('https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io/auth/login', {
            username: username,
            password: 'password123'
        });
        const token = loginRes.data.accessToken;
        console.log("Logged in!");

        const clubData = {
            name: "TestClubRp" + Math.floor(Math.random()*1000),
            acronym: "TCR",
            division: "BRONZE",
            initialRp: 550000
        };

        console.log("Sending POST /clubs with:", clubData);

        const createRes = await axios.post('https://api-gateway.happyrock-6898a204.spaincentral.azurecontainerapps.io/clubs', clubData, {
            headers: { Authorization: `Bearer ${token}` }
        });

        console.log("Created club response:", createRes.data);

    } catch (e) {
        console.error("Error:", e.response ? e.response.data : e.message);
    }
}
main();
