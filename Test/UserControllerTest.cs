using CloudWork.Controllers;
using CloudWork.Model;
using CloudWork.Service.Interface;
using Microsoft.AspNetCore.Mvc;
using Moq;
using Xunit;

namespace Test
{
    public class UserControllerTest
    {
        [Fact]
        public async Task Index_ReturnsAViewResult_WithAListOfUsers()
        {
            // Arrange
            var mockRepo = new Mock<IGenericService<User>>();
            mockRepo.Setup(repo => repo.GetAllAsync())
                .ReturnsAsync(GetTestSessions());
            var controller = new UsersController(mockRepo.Object);
            // Act
            var result = await controller.Index();
            // Assert
            var viewResult = Assert.IsType<ViewResult>(result);
            var model = Assert.IsAssignableFrom<IEnumerable<User>>(
                viewResult.ViewData.Model);
            Assert.Equal(2, model.Count());
        }

        private List<User> GetTestSessions()
        {
            var sessions = new List<User>();
            sessions.Add(new User()
            {
                Id = 1,
                UserName = "Test One"
            });
            sessions.Add(new User()
            {
                Id = 2,
                UserName = "Test Two"
            });
            return sessions;
        }
    }
}