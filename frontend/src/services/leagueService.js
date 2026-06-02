import api from './api';

const leagueService = {
    getAllLeagues: async () => {
        const response = await api.get('/leagues');
        return response.data;
    },

    getStandings: async (leagueId) => {
        const response = await api.get(`/leagues/${leagueId}/standings`);
        return response.data;
    },

    getMatches: async (leagueId, page = 0) => {
        const response = await api.get(`/matches/league/${leagueId}?page=${page}`);
        return response.data;
    }
};

export default leagueService;
