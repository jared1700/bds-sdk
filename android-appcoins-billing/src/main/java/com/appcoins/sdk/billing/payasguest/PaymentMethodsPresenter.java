package com.appcoins.sdk.billing.payasguest;

import android.content.Intent;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.SkuDetails;
import com.appcoins.sdk.billing.WalletInteractListener;
import com.appcoins.sdk.billing.helpers.WalletInstallationIntentBuilder;
import com.appcoins.sdk.billing.listeners.PurchasesListener;
import com.appcoins.sdk.billing.listeners.PurchasesModel;
import com.appcoins.sdk.billing.listeners.SingleSkuDetailsListener;
import com.appcoins.sdk.billing.listeners.payasguest.PaymentMethodsListener;
import com.appcoins.sdk.billing.models.billing.SkuDetailsModel;
import com.appcoins.sdk.billing.models.billing.SkuPurchase;
import com.appcoins.sdk.billing.models.payasguest.PaymentMethod;
import com.appcoins.sdk.billing.models.payasguest.PaymentMethodsModel;
import com.appcoins.sdk.billing.models.payasguest.WalletGenerationModel;

class PaymentMethodsPresenter {

  private final PaymentMethodsView fragmentView;
  private PaymentMethodsInteract paymentMethodsInteract;
  private WalletInstallationIntentBuilder walletInstallationIntentBuilder;

  PaymentMethodsPresenter(PaymentMethodsView view, PaymentMethodsInteract paymentMethodsInteract,
      WalletInstallationIntentBuilder walletInstallationIntentBuilder) {

    this.fragmentView = view;
    this.paymentMethodsInteract = paymentMethodsInteract;
    this.walletInstallationIntentBuilder = walletInstallationIntentBuilder;
  }

  void prepareUi(final BuyItemProperties buyItemProperties) {
    String id = paymentMethodsInteract.retrieveWalletId();
    WalletInteractListener walletInteractListener = new WalletInteractListener() {
      @Override public void walletIdRetrieved(WalletGenerationModel walletGenerationModel) {
        fragmentView.saveWalletInformation(walletGenerationModel);
        provideSkuDetailsInformation(buyItemProperties, walletGenerationModel.hasError());
        checkForUnconsumedPurchased(buyItemProperties.getPackageName(), buyItemProperties.getSku(),
            walletGenerationModel.getWalletAddress(), walletGenerationModel.getSignature(),
            "INAPP");
      }
    };
    paymentMethodsInteract.requestWallet(id, walletInteractListener);
    MaxBonusListener maxBonusListener = new MaxBonusListener() {
      @Override public void onBonusReceived(int bonus) {
        fragmentView.showBonus(bonus);
      }
    };
    paymentMethodsInteract.requestMaxBonus(maxBonusListener);
  }

  void onCancelButtonClicked() {
    fragmentView.close();
  }

  void onPositiveButtonClicked(String selectedRadioButton) {
    if (selectedRadioButton.equals("paypal") || selectedRadioButton.equals("credit_card")) {
      fragmentView.navigateToAdyen(selectedRadioButton);
    } else {
      Intent intent = walletInstallationIntentBuilder.getWalletInstallationIntent();
      if (intent != null) {
        boolean shouldHide = intent.getPackage() != null && intent.getPackage()
            .equals(BuildConfig.APTOIDE_PACKAGE_NAME);
        fragmentView.redirectToWalletInstallation(intent, shouldHide);
      } else {
        fragmentView.showAlertNoBrowserAndStores();
      }
    }
  }

  void onRadioButtonClicked(String selectedRadioButton) {
    fragmentView.setRadioButtonSelected(selectedRadioButton);
    fragmentView.setPositiveButtonText(selectedRadioButton);
  }

  void onErrorButtonClicked() {
    fragmentView.close();
  }

  void onDestroy() {
    paymentMethodsInteract.cancelRequests();
  }

  private void provideSkuDetailsInformation(BuyItemProperties buyItemProperties,
      boolean walletGenerated) {
    if (!walletGenerated) {
      SingleSkuDetailsListener listener = new SingleSkuDetailsListener() {
        @Override public void onResponse(boolean error, SkuDetails skuDetails) {
          if (!error) {
            loadPaymentsAvailable(skuDetails.getFiatPrice(), skuDetails.getFiatPriceCurrencyCode());
            fragmentView.setSkuInformation(new SkuDetailsModel(skuDetails.getFiatPrice(),
                skuDetails.getFiatPriceCurrencyCode(), skuDetails.getAppcPrice(),
                skuDetails.getSku()));
          } else {
            fragmentView.showInstallDialog();
          }
        }
      };
      paymentMethodsInteract.requestSkuDetails(buyItemProperties, listener);
    } else {
      fragmentView.showInstallDialog();
    }
  }

  private void loadPaymentsAvailable(String fiatPrice, String fiatCurrency) {
    PaymentMethodsListener paymentMethodsListener = new PaymentMethodsListener() {
      @Override public void onResponse(PaymentMethodsModel paymentMethodsModel) {
        if (paymentMethodsModel.hasError() || paymentMethodsModel.getPaymentMethods()
            .isEmpty()) {
          fragmentView.showInstallDialog();
        } else {
          for (PaymentMethod paymentMethod : paymentMethodsModel.getPaymentMethods()) {
            if (paymentMethod.isAvailable()) {
              fragmentView.addPayment(paymentMethod.getName());
            }
          }
          fragmentView.showPaymentView();
        }
      }
    };
    paymentMethodsInteract.loadPaymentsAvailable(fiatPrice, fiatCurrency, paymentMethodsListener);
  }

  private void checkForUnconsumedPurchased(String packageName, final String sku,
      String walletAddress, String signature, String type) {
    PurchasesListener purchasesListener = new PurchasesListener() {
      @Override public void onResponse(PurchasesModel purchasesModel) {
        if (!purchasesModel.isError()) {
          for (SkuPurchase skuPurchase : purchasesModel.getSkuPurchases()) {
            if (skuPurchase.getProduct()
                .getName()
                .equals(sku)) {
              paymentMethodsInteract.cancelRequests();
              fragmentView.showItemAlreadyOwnedError(skuPurchase);
              return;
            }
          }
        }
      }
    };
    paymentMethodsInteract.checkForUnconsumedPurchased(packageName, walletAddress, signature,
        type.toLowerCase(), purchasesListener);
  }
}
