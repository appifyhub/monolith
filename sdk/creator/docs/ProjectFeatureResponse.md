# ProjectFeatureResponse

Details about the project's features

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The name of the feature | 
**is_required** | **bool** | Whether the feature is required for the project to run or not | 

## Example

```python
from appifyhub.models.project_feature_response import ProjectFeatureResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ProjectFeatureResponse from a JSON string
project_feature_response_instance = ProjectFeatureResponse.from_json(json)
# print the JSON string representation of the object
print(ProjectFeatureResponse.to_json())

# convert the object into a dict
project_feature_response_dict = project_feature_response_instance.to_dict()
# create an instance of ProjectFeatureResponse from a dict
project_feature_response_from_dict = ProjectFeatureResponse.from_dict(project_feature_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


