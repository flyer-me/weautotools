using CloudWork.Common;
using CloudWork.Repository.UnitOfWork;

namespace CloudWork.Service
{
    public interface IRegisterService
    {
        Task<IEnumerable<string>> GenerateUniqueUserNamesAsync(string userName, int count = 3);
    }

    [Service]
    public class RegisterService : IRegisterService
    {
        private readonly IUnitOfWork _unitOfWork;
        private readonly int MAX_SUGGESTIONS = 10;
        private readonly int MAX_RANDOM = 1000;
        public RegisterService(IUnitOfWork unitOfWork)
        {
            _unitOfWork = unitOfWork;
        }
        /// <summary>
        /// 生成用户名建议：用户名+随机数字，生成count（默认3）个
        /// </summary>
        /// <param name="userNameBase"></param>
        /// <param name="count"></param>
        /// <returns></returns>
        public async Task<IEnumerable<string>> GenerateUniqueUserNamesAsync(string userNameBase, int count = 3)
        {
            var suggestions = new List<string>();

            if (count <= 0 || string.IsNullOrWhiteSpace(userNameBase))
            {
                return suggestions;
            }

            if (count > MAX_SUGGESTIONS)
            {
                count = MAX_SUGGESTIONS;
            }

            for (int i = 0, max = 0; i < count && max < MAX_RANDOM; )
            {
                var suggestion = userNameBase + new Random().Next(100, 1000);
                if (await _unitOfWork.Users.AnyAsync(u => u.UserName == suggestion))
                {
                    continue;
                }
                suggestions.Add(suggestion);
                i++;
            }

            return await Task.FromResult(suggestions);
        }
    }
}
