import api from './api';

const leagueService = {
    getAllLeagues: async () => {
        const response = await api.get('/leagues');
        return response.data;
    },

    getLeagueById: async (leagueId) => {
        const response = await api.get(`/leagues/${leagueId}`);
        return response.data;
    },

    getStandings: async (leagueId) => {
        const response = await api.get(`/leagues/${leagueId}/standings`);
        return response.data;
    },

    getLeagueMatches: async (leagueId) => {
        const response = await api.get(`/matches/league/${leagueId}?size=100`);
        return response.data;
    },

    generateTournament: async (leagueId) => {
        const response = await api.post(`/matches/league/${leagueId}/tournament`);
        return response.data;
    },

    acceptWager: async (matchId, clubId) => {
        const response = await api.post(`/matches/${matchId}/wager/accept?clubId=${clubId}`);
        return response.data;
    },

    getMatches: async (leagueId, page = 0) => {
        const response = await api.get(`/matches/league/${leagueId}?page=${page}`);
        return response.data;
    },

    createLeague: async (leagueData) => {
        const response = await api.post('/leagues', leagueData);
        return response.data;
    },

    enrollClub: async (leagueId, clubId) => {
        const response = await api.post(`/leagues/${leagueId}/enroll?clubId=${clubId}`);
        return response.data;
    },

    getLeaguesByClub: async (clubId) => {
        const response = await api.get(`/leagues/by-club?clubId=${clubId}`);
        return response.data;
    },

    joinLeague: async (leagueId) => {
        const response = await api.post(`/leagues/${leagueId}/join`);
        return response.data;
    },

    getMyLeagues: async () => {
        const response = await api.get('/leagues/my-leagues');
        return response.data;
    },

    getWonLeaguesCount: async () => {
        const response = await api.get('/leagues/won-count');
        return response.data;
    }
};

export default leagueService;

