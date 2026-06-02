const testJoinLeague = async () => {
    try {
        const loginRes = await fetch('http://localhost:8080/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: 'admin', password: 'Admin1234!' })
        });
        const loginData = await loginRes.json();
        const token = loginData.accessToken;
        
        // 1. Create club
        const clubRes = await fetch('http://localhost:8080/clubs', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({
                name: 'Join Test Club',
                acronym: 'JTC',
                division: 'BRONZE'
            })
        });
        const clubData = await clubRes.json();
        console.log('Created club:', clubData);

        // 2. Enroll in league 2
        const enrollRes = await fetch('http://localhost:8080/leagues/2/enroll?clubId=' + clubData.id, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            }
        });
        const enrollText = await enrollRes.text();
        console.log('Enroll status:', enrollRes.status);
        console.log('Enroll Response:', enrollText);
    } catch (e) {
        console.error('Fetch error:', e.message);
    }
}
testJoinLeague();
