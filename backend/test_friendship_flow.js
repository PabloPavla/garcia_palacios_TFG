const mysql = require('mysql2/promise');

async function testFriendshipFlow() {
    console.log("=== STARTING FRIENDSHIP STATUS ENDPOINT TEST ===");
    const gatewayUrl = 'http://localhost:8080';

    try {
        // 1. Reset friendships in database first
        const connection = await mysql.createConnection({
            host: 'localhost',
            user: 'tfg_user',
            password: 'tfg_pass',
            database: 'auth_db'
        });
        await connection.execute('DELETE FROM friendships');
        console.log("Deleted all existing friendships for clean testing.");
        await connection.end();

        // 2. Login as testuser (ID 2)
        console.log("\nLogging in as testuser...");
        let loginRes = await fetch(`${gatewayUrl}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: 'testuser', password: '123456' })
        });
        let loginData = await loginRes.json();
        const testUserToken = loginData.accessToken;
        console.log("testuser logged in successfully.");

        // 3. Get status of admin (ID 1) from testuser perspective
        console.log("\nChecking friendship status with admin (should be NONE)...");
        let statusRes = await fetch(`${gatewayUrl}/auth/friends/status/1`, {
            headers: { 'Authorization': `Bearer ${testUserToken}` }
        });
        console.log("Response status:", statusRes.status);
        let statusText = await statusRes.text();
        console.log("Response text:", statusText);
        let statusData = JSON.parse(statusText);
        console.log("Status response:", statusData);
        if (statusData.friendshipStatus !== 'NONE') {
            throw new Error(`Expected NONE but got ${statusData.friendshipStatus}`);
        }

        // 4. Send friend request to admin
        console.log("\nSending friend request to admin...");
        let reqRes = await fetch(`${gatewayUrl}/auth/friends/request`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${testUserToken}` 
            },
            body: JSON.stringify({ username: 'admin' })
        });
        console.log("Request status code:", reqRes.status);

        // 5. Get status of admin (should be PENDING_SENT)
        console.log("\nChecking friendship status with admin again (should be PENDING_SENT)...");
        statusRes = await fetch(`${gatewayUrl}/auth/friends/status/1`, {
            headers: { 'Authorization': `Bearer ${testUserToken}` }
        });
        statusData = await statusRes.json();
        console.log("Status response:", statusData);
        if (statusData.friendshipStatus !== 'PENDING_SENT') {
            throw new Error(`Expected PENDING_SENT but got ${statusData.friendshipStatus}`);
        }
        const friendshipId = statusData.friendshipId;

        // 6. Login as admin
        console.log("\nLogging in as admin...");
        loginRes = await fetch(`${gatewayUrl}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: 'admin', password: 'Admin1234!' })
        });
        loginData = await loginRes.json();
        const adminToken = loginData.accessToken;
        console.log("admin logged in successfully.");

        // 7. Get status of testuser (ID 2) from admin perspective (should be PENDING_RECEIVED)
        console.log("\nChecking friendship status with testuser (should be PENDING_RECEIVED)...");
        statusRes = await fetch(`${gatewayUrl}/auth/friends/status/2`, {
            headers: { 'Authorization': `Bearer ${adminToken}` }
        });
        statusData = await statusRes.json();
        console.log("Status response:", statusData);
        if (statusData.friendshipStatus !== 'PENDING_RECEIVED') {
            throw new Error(`Expected PENDING_RECEIVED but got ${statusData.friendshipStatus}`);
        }

        // 8. Accept request
        console.log(`\nAccepting request with ID ${friendshipId}...`);
        let acceptRes = await fetch(`${gatewayUrl}/auth/friends/${friendshipId}/accept`, {
            method: 'PUT',
            headers: { 'Authorization': `Bearer ${adminToken}` }
        });
        console.log("Accept status code:", acceptRes.status);

        // 9. Get status of testuser from admin perspective (should be ACCEPTED)
        console.log("\nChecking status after accepting (should be ACCEPTED)...");
        statusRes = await fetch(`${gatewayUrl}/auth/friends/status/2`, {
            headers: { 'Authorization': `Bearer ${adminToken}` }
        });
        statusData = await statusRes.json();
        console.log("Status response:", statusData);
        if (statusData.friendshipStatus !== 'ACCEPTED') {
            throw new Error(`Expected ACCEPTED but got ${statusData.friendshipStatus}`);
        }

        console.log("\n=== ALL BACKEND TESTS PASSED SUCCESSFULLY ===");

     } catch (err) {
        console.error("Test failed:", err.message);
    }
}

testFriendshipFlow();
