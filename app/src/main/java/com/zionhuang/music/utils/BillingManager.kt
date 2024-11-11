package com.zionhuang.music.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.PurchasesUpdatedListener
import timber.log.Timber

class BillingManager(private val context: Context) : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    var onPurchaseComplete: (() -> Unit)? = null

    companion object {
        const val TAG = "BillingManager"
        const val REMOVE_ADS_SKU = "adsdisabled" // ID de producto en Google Play Console
    }

    init {
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Timber.tag(TAG).d("Conexión de facturación desconectada")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Timber.tag(TAG).d("Conexión de facturación exitosa")
                    checkPurchases() // Verifica si la compra ya fue hecha
                }
            }
        })
    }

    // Método para iniciar la compra
    fun initiatePurchase(activity: Activity) {
        val skuList = listOf(REMOVE_ADS_SKU)
        val params = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(BillingClient.SkuType.INAPP).build()

        billingClient?.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                for (skuDetails in skuDetailsList) {
                    if (skuDetails.sku == REMOVE_ADS_SKU) {
                        val purchaseParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .build()
                        billingClient?.launchBillingFlow(activity, purchaseParams)
                    }
                }
            } else {
                Timber.tag(TAG).d("No se encontraron productos para comprar")
            }
        }
    }

    // Implementación de la interfaz PurchasesUpdatedListener
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Timber.tag(TAG).d("Compra cancelada por el usuario")
        } else {
            Timber.tag(TAG).d("Error en la compra: " + billingResult.debugMessage)
        }
    }

    // Maneja la compra y verifica el recibo
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.products.firstOrNull() == REMOVE_ADS_SKU && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        onPurchaseComplete?.invoke()
                    }
                }
            } else {
                onPurchaseComplete?.invoke()
            }
        }
    }

    // Verifica el estado de la compra para ocultar anuncios si ya se ha comprado
    fun checkPurchases() {
        billingClient?.queryPurchasesAsync(BillingClient.SkuType.INAPP) { _, purchases ->
            for (purchase in purchases) {
                if (purchase.products.firstOrNull() == REMOVE_ADS_SKU) {
                    onPurchaseComplete?.invoke()
                }
            }
        }
    }

    // Cierra la conexión de facturación
    fun endBillingConnection() {
        billingClient?.endConnection()
    }
}
