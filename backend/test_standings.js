const testStandings = async () => {
    try {
        const loginRes = await fetch('http://localhost:8080/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: 'admin', password: 'Admin1234!' })
        });
        const loginData = await loginRes.json();
        const token = loginData.accessToken;
        
        const res = await fetch('http://localhost:8080/leagues/2/standings', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            }
        });
        const text = await res.text();
        console.log('Status:', res.status);
        console.log('Response:', text);
    } catch (e) {
        console.error('Fetch error:', e.message);
    }
}
testStandings();
