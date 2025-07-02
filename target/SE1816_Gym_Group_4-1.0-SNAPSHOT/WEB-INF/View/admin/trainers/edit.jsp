
<style>
    .modal {
        display: none;
        position: fixed;
        z-index: 1000;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        justify-content: center;
        align-items: center;
        overflow: auto;
    }

    .modal--active {
        display: flex;
    }

    .modal-content {
        background-color: #fff;
        border-radius: 12px;
        padding: 30px 40px;
        width: 400px;
        max-width: 90%;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
        animation: fadeInScale 0.3s ease;
    }

    @keyframes fadeInScale {
        from {
            opacity: 0;
            transform: scale(0.9);
        }
        to {
            opacity: 1;
            transform: scale(1);
        }
    }

    .modal-content h2 {
        margin-bottom: 20px;
        font-size: 22px;
        color: #333;
        text-align: center;
    }

    .modal-content label {
        font-weight: bold;
        margin-bottom: 6px;
        display: block;
        color: #444;
    }

    .modal-content input[type="text"],
    .modal-content input[type="password"],
    .modal-content input[type="file"],
    .modal-content select {
        width: 100%;
        padding: 10px;
        margin-bottom: 16px;
        border-radius: 6px;
        border: 1px solid #ccc;
        font-size: 14px;
    }

    .modal-content button[type="submit"],
    .modal-content button[type="button"] {
        padding: 10px 20px;
        border: none;
        border-radius: 6px;
        font-size: 14px;
        cursor: pointer;
        margin-right: 10px;
    }

    .modal-content button[type="submit"] {
        background-color: #4CAF50;
        color: white;
    }

    .modal-content button[type="submit"]:hover {
        background-color: #45a049;
    }

    .modal-content button[type="button"] {
        background-color: #ccc;
    }

    .modal-content button[type="button"]:hover {
        background-color: #bbb;
    }

</style>
<div class="modal" id="editTrainerModal">
    <div class="modal-content">
        <h2>Edit Trainer</h2>
        <form method="post"
              action="/SE1816_Gym_Group_4/TrainerServlet"
              enctype="multipart/form-data"
              onsubmit="return submitEditTrainerForm(this, 'editTrainerResult');">
            <input type="hidden" name="formAction" value="edit">
            <input type="hidden" name="trainerId" id="editTrainerId">

            <label for="editTrainerFullName">Full Name:</label>
            <input type="text" name="fullname" id="editTrainerFullName" required>

            <label for="editTrainerEmail">Email:</label>
            <input type="text" name="email" id="editTrainerEmail" required>

            <label for="editTrainerPhone">Phone:</label>
            <input type="text" name="phone_number" id="editTrainerPhone">

            <label for="editTrainerBio">Bio:</label>
            <input type="text" name="bio" id="editTrainerBio">

            <label for="editTrainerExperience">Experience (years):</label>
            <input type="text" name="experience_years" id="editTrainerExperience">
            <label for="editTrainerPrice">Session Price (VND):</label>
            <input type="text" name="price" id="editTrainerPrice" required>

            <label for="editTrainerRating">Rating:</label>
            <input type="text" name="rating" id="editTrainerRating">

            <button type="submit">Save</button>
            <button type="button" onclick="closeModal('editTrainerModal')">Cancel</button>
        </form>
        <div id="editTrainerResult" style="margin-top: 10px;"></div>
    </div>
</div>
