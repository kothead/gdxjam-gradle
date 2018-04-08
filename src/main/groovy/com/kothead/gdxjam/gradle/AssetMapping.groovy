import groovy.transform.Canonical

@Canonical
class AssetMapping {
    File file
    String fieldName
    String fileName
    Class assetType
    Class paramType
    String params
}
