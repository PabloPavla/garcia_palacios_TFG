import api from './api';

const transferService = {
    getTransferById: async (id) => {
        const response = await api.get(`/transfers/${id}`);
        return response.data;
    },

    getClubBuyingHistory: async (clubId, page = 0) => {
        const response = await api.get(`/transfers/club/${clubId}/buying?page=${page}`);
        return response.data;
    },

    getClubSellingHistory: async (clubId, page = 0) => {
        const response = await api.get(`/transfers/club/${clubId}/selling?page=${page}`);
        return response.data;
    },

    createTransferOffer: async (playerId, fee, toClubId, exchangePlayerId = null) => {
        const response = await api.post(`/transfers`, {
            playerId: playerId,
            toClubId: toClubId,
            transferFeeRp: fee,
            exchangePlayerId: exchangePlayerId
        });
        return response.data;
    },

    acceptOffer: async (transferId, clubId) => {
        const response = await api.put(`/transfers/${transferId}/accept?clubId=${clubId}`);
        return response.data;
    },

    rejectOffer: async (transferId, clubId) => {
        const response = await api.put(`/transfers/${transferId}/reject?clubId=${clubId}`);
        return response.data;
    },

    cancelOffer: async (transferId, clubId) => {
        const response = await api.put(`/transfers/${transferId}/cancel?clubId=${clubId}`);
        return response.data;
    },

    counterOffer: async (transferId, fee, exchangePlayerId, clubId) => {
        const response = await api.put(`/transfers/${transferId}/counter?clubId=${clubId}`, {
            playerId: null, // no se usa
            toClubId: null, // no se usa
            transferFeeRp: fee,
            exchangePlayerId: exchangePlayerId
        });
        return response.data;
    }
};

export default transferService;
