# Allow service providers to edit and delete services

This plan covers enabling service providers to manage their services by adding edit and delete capabilities to the "My Services" screen.

## User Review Required

> [!NOTE]
> I will add a confirmation dialog for deletion to prevent accidental data loss.
> The "Edit" functionality will be accessible by clicking the service card in the list, and I'll also add an explicit Edit icon for clarity.

## Proposed Changes

### UI Components

#### [MODIFY] [MyServicesScreen.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/screens/MyServicesScreen.kt)
- Make `MyServiceCard` clickable to trigger the edit navigation.
- Add an `Edit` icon to the card.
- Implement a `DeleteConfirmationDialog` to confirm before deleting a service.
- Update `MyServicesScreen` to handle the deletion confirmation logic.

### ViewModels

#### [MODIFY] [MyServicesViewModel.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/viewmodels/MyServicesViewModel.kt)
- Ensure `deleteService` works as expected (already implemented, but will verify).

#### [MODIFY] [PostServiceViewModel.kt](file:///C:/Users/thabi/StudioProjects/hustlefix/app/src/main/java/com/example/hustlefix/ui/viewmodels/PostServiceViewModel.kt)
- Refine `updateService` to handle image updates correctly without losing other data.

## Verification Plan

### Manual Verification
- Navigate to "Business Center" -> "My Services".
- Tap on a service card to open the "Edit Service" screen.
- Modify the title/price/description and save. Verify the changes are reflected in the list.
- Tap the "Delete" icon on a service card.
- Verify the confirmation dialog appears.
- Confirm deletion and verify the service is removed from the list and Firebase.
