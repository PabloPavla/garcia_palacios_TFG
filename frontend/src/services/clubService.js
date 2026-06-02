import api from './api';

const clubService = {
    getAllClubs: async () => {
        const response = await api.get('/clubs');
        return response.data;
    },
    
    getClubById: async (id) => {
        const response = await api.get(`/clubs/${id}`);
        return response.data;
    },

    getMyClub: async () => {
        const response = await api.get('/clubs/my-club');
        return response.data;
    },

    createClub: async (clubData) => {
        const response = await api.post('/clubs', clubData);
        return response.data;
    },

    getClubPlayers: async (clubId) => {
        const response = await api.get(`/clubs/${clubId}/players`);
        return response.data;
    },

    getFreeAgents: async (page = 0, size = 20) => {
        const response = await api.get(`/players?page=${page}&size=${size}`);
        return response.data;
    },
    
    getPlayerById: async (id) => {
        const response = await api.get(`/players/${id}`);
        return response.data;
    }
};

export default clubService;
