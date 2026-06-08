import api from './api';

const friendService = {
    searchUsers: async (query) => {
        const response = await api.get(`/auth/friends/search?q=${query}`);
        return response.data;
    },

    sendRequest: async (username) => {
        const response = await api.post('/auth/friends/request', { username });
        return response.data;
    },

    acceptRequest: async (friendshipId) => {
        const response = await api.put(`/auth/friends/${friendshipId}/accept`);
        return response.data;
    },

    rejectRequest: async (friendshipId) => {
        const response = await api.put(`/auth/friends/${friendshipId}/reject`);
        return response.data;
    },

    removeFriend: async (friendshipId) => {
        const response = await api.delete(`/auth/friends/${friendshipId}`);
        return response.data;
    },

    getFriends: async () => {
        const response = await api.get('/auth/friends');
        return response.data;
    },

    getPendingRequests: async () => {
        const response = await api.get('/auth/friends/pending');
        return response.data;
    },

    getFriendshipStatus: async (targetUserId) => {
        const response = await api.get(`/auth/friends/status/${targetUserId}`);
        return response.data;
    }
};

export default friendService;
