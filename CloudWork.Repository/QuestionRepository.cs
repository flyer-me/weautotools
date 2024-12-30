using CloudWork.Common.DB;
using CloudWork.Model;
using CloudWork.Repository.Base;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudWork.Repository
{
    public interface IQuestionRepository : IBaseRepository<Question> { }
    public class QuestionRepository : BaseRepository<Question>, IQuestionRepository
    {
        public QuestionRepository(CloudWorkDbContext context) : base(context) { }
    }
}
