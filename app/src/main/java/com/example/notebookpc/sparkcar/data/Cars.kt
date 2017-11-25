package com.example.notebookpc.sparkcar.data

/**
 * Created by NOTEBOOK pC on 11/25/2017.
 */
class Cars {

    private lateinit var id: String
    private lateinit var ownerId: String
    private lateinit var name: String
    private lateinit var carPlate: String
    private lateinit var color: String

    constructor(id: String, ownerId: String, name: String, carPlate: String, color: String) {
        this.id = id
        this.ownerId = ownerId
        this.name = name
        this.carPlate = carPlate
        this.color = color
    }
}