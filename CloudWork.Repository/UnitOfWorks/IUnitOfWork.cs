using CloudWork.Repository.Base;

namespace CloudWork.Repository.UnitOfWorks
{
    public interface IUnitOfWork : IDisposable
    {
        IBaseRepository<TEntity> Repository<TEntity>() where TEntity : class;
        //TestCaseRepository TestCases { get; }

        void BeginTransaction();
        void CommitTransaction();
        void RollbackTransaction();
        Task<int> SaveAsync();
    }
}
