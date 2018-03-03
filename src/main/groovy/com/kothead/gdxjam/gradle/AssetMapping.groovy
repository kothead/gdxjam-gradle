import groovy.transform.Canonical

@Canonical
class AssetMapping {
    String fieldName
    File file
    Class assetType
    Class paramType
    String params
}
