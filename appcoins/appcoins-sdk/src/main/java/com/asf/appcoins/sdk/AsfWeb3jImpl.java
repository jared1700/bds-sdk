package com.asf.appcoins.sdk;

import com.asf.appcoins.sdk.entity.Transaction;
import com.asf.appcoins.sdk.entity.Transaction.Status;
import com.asf.appcoins.sdk.entity.factory.TransactionFactory;
import io.reactivex.Observable;
import java.math.BigDecimal;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthTransaction;

/**
 * Created by neuro on 26-02-2018.
 */

final class AsfWeb3jImpl implements AsfWeb3j {

  private final Web3j web3j;

  AsfWeb3jImpl(Web3j web3j) {
    this.web3j = web3j;
  }

  @Override public Observable<Long> getNonce(Address address) {
    return Observable.fromCallable(() -> web3j.ethGetTransactionCount(address.getTypeAsString(),
        DefaultBlockParameterName.PENDING)
        .send())
        .map(ethGetTransactionCount -> ethGetTransactionCount.getTransactionCount()
            .longValue());
  }

  @Override public Observable<Long> getGasPrice(Address address) {
    return Observable.fromCallable(() -> web3j.ethGasPrice()
        .send())
        .map(ethGasPrice -> ethGasPrice.getGasPrice()
            .longValue());
  }

  @Override public Observable<BigDecimal> getBalance(Address address) {
    throw new RuntimeException("Not implemented yet");
  }

  @Override public Observable<String> sendRawTransaction(String rawData) {
    return Observable.fromCallable(() -> web3j.ethSendRawTransaction(rawData)
        .send())
        .map(EthSendTransaction::getTransactionHash);
  }

  @Override public Observable<Transaction> getTransactionByHash(String txHash) {
    return Observable.fromCallable(() -> web3j.ethGetTransactionReceipt(txHash)
        .send())
        .flatMap(ethGetTransactionReceipt -> {
          if ((ethGetTransactionReceipt == null)
              || (ethGetTransactionReceipt.getTransactionReceipt() == null)) {
            EthTransaction send = web3j.ethGetTransactionByHash(txHash)
                .send();
            return Observable.just(TransactionFactory.fromEthTransaction(send, Status.PENDING));
          } else {
            return Observable.just(
                TransactionFactory.fromEthGetTransactionReceipt(ethGetTransactionReceipt));
          }
        });
  }
}
