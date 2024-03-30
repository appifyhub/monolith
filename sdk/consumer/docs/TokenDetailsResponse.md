# TokenDetailsResponse

The decoded details of a token

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**token_value** | **str** | The value of the token | 
**user_id** | **str** | A unique user identifier within the project. Depending on the project configuration, it can be differently formatted or even auto-generated.  | 
**project_id** | **int** | A unique project identifier | 
**universal_id** | **str** | The universal ID of the object (user ID and project ID separated by a dollar sign) | 
**created_at** | **str** | The time the object was created (based on ISO 8601) | 
**expires_at** | **str** | The time the token expires (based on ISO 8601) | 
**authority** | [**Authority**](Authority.md) |  | 
**is_blocked** | **bool** | Whether the token is blocked or not | 
**origin** | **str** | The origin of the request | [optional] 
**ip_address** | **str** | The approximate IP address | [optional] 
**geo** | **str** | The approximate geographic location based on the IP address | [optional] 
**is_static** | **bool** | Whether the token is static or not (i.e. API key) | 

## Example

```python
from appifyhub.models.token_details_response import TokenDetailsResponse

# TODO update the JSON string below
json = "{}"
# create an instance of TokenDetailsResponse from a JSON string
token_details_response_instance = TokenDetailsResponse.from_json(json)
# print the JSON string representation of the object
print(TokenDetailsResponse.to_json())

# convert the object into a dict
token_details_response_dict = token_details_response_instance.to_dict()
# create an instance of TokenDetailsResponse from a dict
token_details_response_form_dict = token_details_response.from_dict(token_details_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


