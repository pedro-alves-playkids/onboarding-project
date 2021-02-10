package com.playkids.onboarding.core.service

import com.playkids.onboarding.core.excption.EntityNotFoundException
import com.playkids.onboarding.core.excption.NotEnoughCurrencyException
import com.playkids.onboarding.core.excption.UserHasItemException
import com.playkids.onboarding.core.model.*
import com.playkids.onboarding.core.persistence.ItemDAO
import com.playkids.onboarding.core.persistence.ProfileDAO
import com.playkids.onboarding.core.persistence.SKUDAO
import com.playkids.onboarding.core.util.ProfileCurrencies

class ProfileService(
    private val profileDAO: ProfileDAO,
    private val skuDAO: SKUDAO,
    private val itemDAO: ItemDAO
) {
    suspend fun create(profile: Profile){
        profileDAO.create(profile)
    }

    suspend fun find(id: ProfileId): Profile?{
        return profileDAO.find(id)
    }

    suspend fun addItem(profileId: ProfileId, itemId: ItemId, itemCategory: String){
        return profileDAO.addItem(profileId, listOf("$itemCategory:$itemId"))
    }

    suspend fun buyItem(profileId: ProfileId, itemId: ItemId, itemCategory: String){
        val item = itemDAO.find(itemCategory, itemId) ?: throw EntityNotFoundException("Item with id $itemId and category $itemCategory doesn't exists")
        val itemKey = ItemKey.fromItem(item)
        val (profileItems, currencyAmount) = profileDAO.getItemsAndCurrency(profileId, item.currency) ?: throw EntityNotFoundException("profile with id $profileId doesn't exists")
        if (item.price > currencyAmount) throw NotEnoughCurrencyException("profile with id $profileId doesn't have enough ${item.currency} to buy item")
        if (itemKey.getKey() in profileItems.map { it.getKey() }) throw UserHasItemException("profile with id $profileId already has item of id $itemId")
        profileDAO.updateCurrency(profileId, item.currency, (-item.price))
        profileDAO.addItem(profileId, listOf("$itemCategory:$itemId"))
    }

    suspend fun addSku(profileId: ProfileId, skuId: SKUId){
        val sku = skuDAO.find(skuId) ?: throw EntityNotFoundException("SKU with id $skuId doesn't exists")
        //{CHAMADA DE API PARA VERIFICAR A COMPRA}
        profileDAO.updateCurrency(profileId, ProfileCurrencies.COIN, sku.coin)
        profileDAO.updateCurrency(profileId, ProfileCurrencies.GEM, sku.gem)
        profileDAO.updateCurrency(profileId, ProfileCurrencies.MONEY, sku.price)
    }
}