package com.playkids.onboarding.dynamodb

import com.movile.kotlin.commons.dynamodb.*
import com.playkids.onboarding.core.model.ItemId
import com.playkids.onboarding.core.model.Profile
import com.playkids.onboarding.core.model.ProfileId
import com.playkids.onboarding.core.persistence.ProfileDAO
import com.playkids.onboarding.core.util.ChooseValue
import com.playkids.onboarding.dynamodb.extensions.itemOrNull
import com.playkids.onboarding.dynamodb.extensions.listOfString
import com.playkids.onboarding.dynamodb.extensions.toListAttributeValue
import com.typesafe.config.Config
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest

class DynamoDBProfileDAO(config: Config, private val dynamoDbClient: DynamoDbAsyncClient): ProfileDAO {

    private val tableName = "onboarding-pedro-profile"

    override suspend fun create(profile: Profile) {
        dynamoDbClient.putItem {
            it.tableName(tableName)
                .conditionExpression("attribute_not_exists($ID)")
                .item(profile.toItem())
        }.awaitRaiseException()
    }

    override suspend fun find(id: ProfileId): Profile? =
        dynamoDbClient.getItem {
            it.tableName(tableName)
                .key(mapOf(ID to id.toAttributeValue()))
        }
            .awaitRaiseException()
            ?.itemOrNull()
            ?.toProfile()

    override suspend fun addItem(profileId: ProfileId, itemId: List<ItemId>) {
        //TODO: RECEBER CATEGORIA DO ITEM TBM E ESTRUTURAR AMBOS
        dynamoDbClient.updateItem(
            UpdateItemRequest.builder()
                .tableName(tableName)
                .key(mapOf(ID to profileId.toAttributeValue()))
                .updateExpression("SET #items = list_append( #items, :val)")
                .expressionAttributeNames(mapOf("#items" to ITEMS))
                .expressionAttributeValues(mapOf(":val" to itemId.toListAttributeValue()))
                .build()
        )
            .awaitRaiseException()
    }

    override suspend fun updateCurrency(profileId: ProfileId, operation: String, currency: String, chooseValue: ChooseValue) {
        dynamoDbClient.updateItem(
            UpdateItemRequest.builder()
                .tableName(tableName)
                .key(mapOf(ID to profileId.toAttributeValue()))
                .updateExpression("SET #currency = #currency $operation :v")
                .expressionAttributeNames(mapOf("#currency" to currency))
                .expressionAttributeValues(mapOf(":v" to chooseValue.chooseToAttributeValue()))
                .build()
        )
            .awaitRaiseException()
    }


    private fun Profile.toItem(): Map<String, AttributeValue> =
        mapOf(
            ID to id.toAttributeValue(),
            USERNAME to username.toAttributeValue(),
            ITEMS to items.toListAttributeValue(),
            COIN to coin.toAttributeValue(),
            GEM to gem.toAttributeValue(),
            MONEY_SPENT to moneySpent.toAttributeValue()
        )


    private fun Map<String, AttributeValue>.toProfile(): Profile =
        Profile(
            id = string(ID)!!,
            username = string(USERNAME)!!,
            items =  listOfString(ITEMS)!!,
            coin = int(COIN)!!,
            gem = int(GEM)!!,
            moneySpent = float(MONEY_SPENT)!!
        )

    companion object {
        private const val ID = "id"
        private const val USERNAME = "username"
        private const val ITEMS = "items"
        private const val COIN = "coin"
        private const val GEM = "gem"
        private const val MONEY_SPENT = "moneySpent"
    }
}