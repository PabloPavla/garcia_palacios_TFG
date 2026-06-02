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

    createTransferOffer: async (playerId, fee) => {
        const response = await api.post(`/transfers`, {
            playerId: playerId,
            transferFeeRp: fee
        });
        return response.data;
    },

    acceptOffer: async (transferId) => {
        const response = await api.put(`/transfers/${transferId}/accept`);
        return response.data;
    },

    rejectOffer: async (transferId) => {
        const response = await api.put(`/transfers/${transferId}/reject`);
        return response.data;
    },

    cancelOffer: async (transferId) => {
        const response = await api.put(`/transfers/${transferId}/cancel`);
        return response.data;
    }
};

export default transferService;
