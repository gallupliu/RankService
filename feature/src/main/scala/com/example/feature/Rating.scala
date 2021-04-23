package com.example.feature

//  物品信息
case class Item(item_id: String)

//  用户-物品-评分
case class Rating(user_id: String, item_id: String, rating: Double)

//  用户信息
case class User(user_id: String)
