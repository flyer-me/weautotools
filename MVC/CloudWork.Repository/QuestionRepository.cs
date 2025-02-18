using CloudWork.Model;
using CloudWork.Repository.Base;
using CloudWork.Repository.DB;

namespace CloudWork.Repository
{
    public interface IQuestionRepository : IBaseRepository<Question> { }
    public class QuestionRepository : BaseRepository<Question>, IQuestionRepository
    {
        public QuestionRepository(AppDbContext context) : base(context) { }
    }
}
