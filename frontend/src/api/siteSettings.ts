import api from './client';

export const siteSettingsApi = {
  getPopularDefaultTab: () =>
    api.get<{ data: { value: string } }>('/site-settings/popular-default-tab'),

  setPopularDefaultTab: (value: string) =>
    api.put('/admin/site-settings/popular-default-tab', { value }),

  getHeroImage: () =>
    api.get<{ data: { imageUrl: string; fallback: boolean } }>('/site-settings/hero-image'),
};
