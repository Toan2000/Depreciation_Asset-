package com.example.assetService.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "asset_type")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssetType {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String assetName;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "\"asset_group_id\"")
    private AssetGroup assetGroup;
}
