package com.appcoins.sdk.billing.payasguest;

import com.appcoins.sdk.billing.SharedPreferencesRepository;
import com.appcoins.sdk.billing.mappers.GamificationMapper;
import com.appcoins.sdk.billing.models.GamificationModel;
import com.appcoins.sdk.billing.service.BdsService;
import com.appcoins.sdk.billing.service.RequestResponse;
import com.appcoins.sdk.billing.service.ServiceResponseListener;
import java.util.ArrayList;
import java.util.HashMap;

class GamificationInteract {
  private final SharedPreferencesRepository sharedPreferencesRepository;
  private final GamificationMapper gamificationMapper;
  private final BdsService bdsService;

  GamificationInteract(SharedPreferencesRepository sharedPreferencesRepository,
      GamificationMapper gamificationMapper, BdsService bdsService) {
    this.sharedPreferencesRepository = sharedPreferencesRepository;
    this.gamificationMapper = gamificationMapper;
    this.bdsService = bdsService;
  }

  void loadMaxBonus(final MaxBonusListener maxBonusListener) {
    if (sharedPreferencesRepository.hasSavedBonus(System.currentTimeMillis())) {
      maxBonusListener.onBonusReceived(
          new GamificationModel(sharedPreferencesRepository.getMaxBonus()));
    } else {
      ServiceResponseListener serviceResponseListener = new ServiceResponseListener() {
        @Override public void onResponseReceived(RequestResponse requestResponse) {
          GamificationModel gamificationModel = gamificationMapper.mapToMaxBonus(requestResponse);
          maxBonusListener.onBonusReceived(gamificationModel);
        }
      };
      bdsService.makeRequest("/gamification/levels", "GET", new ArrayList<String>(),
          new HashMap<String, String>(), new HashMap<String, String>(),
          new HashMap<String, Object>(), serviceResponseListener);
    }
  }

  public void cancelRequests() {
    bdsService.cancelRequests();
  }

  void setMaxBonus(int maxBonus) {
    sharedPreferencesRepository.setMaxBonus(maxBonus);
  }
}
